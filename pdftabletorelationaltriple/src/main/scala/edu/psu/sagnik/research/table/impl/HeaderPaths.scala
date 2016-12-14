package edu.psu.sagnik.research.table.impl

import java.io.File

import edu.psu.sagnik.research.allenaiconversion.AllenAIDataConversion
import edu.psu.sagnik.research.pdsimplify.impl.ProcessDocument
import edu.psu.sagnik.research.table.tablecellextraction.{CellRenaming, CombineWords}
import edu.psu.sagnik.research.table.tripleextraction.TabletoWFT
import org.allenai.common.Logging
import org.allenai.pdffigures2.FigureExtractor
import org.apache.pdfbox.pdmodel.PDDocument

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/** Created by schoudhury on 7/25/16.
  */
object HeaderPaths extends Logging {

  def apply(pdfLoc: String, jsonDir: String, jsonBase: String) = {
    val pdDoc = PDDocument.load(new File(pdfLoc))
    val simpleDocument = Try(ProcessDocument(pdDoc)) match {
      case Success(document) => Some(document);
      case Failure(e) => {
        System.err.println(s"[PDSimplify failed]: ${e.getMessage}")
        None
      }
    }
    pdDoc.close()
    simpleDocument match {
      case Some(doc) =>
        val figureExtractor = FigureExtractor()
        //create rasterized figures anyway
        val allenAIDoc = Try(figureExtractor.getRasterizedFiguresWithText(PDDocument.load(new File(pdfLoc)), dpi = 72)) match {
          case Success(allenAIDoc) => Some(allenAIDoc);
          case Failure(e) => {
            System.err.println(s"[PDSimplify failed]: ${e.getMessage}")
            None
          }
        }

        allenAIDoc match {
          case Some (allenAIDoc) =>

            val myTables = AllenAIDataConversion.tableFromPDFFigures2 (allenAIDoc)
              .map (
                atable => AllenAIDataConversion.allenAITableToMyTable (atable, Some (doc.pages (atable.Page) ) )
              )

            myTables.foreach {
              myTable =>
                myTable match {
                  case Some (properTable) =>
                    val interimTable = CombineWords.wordMergedTable (properTable)
                    val table = CellRenaming.produceRowColNumbers (interimTable)
                    TabletoWFT.headerPathstoDataCells (table) match {
                      case Some (wft) =>
                        val jsonLoc =
                          if (table.cells.length != interimTable.textSegments.length) {
                            logger.warn (s"Interim table and Cell Renamed Tables didn't match. " +
                              s"Created wft with high Error Probability. [pdf]: $pdfLoc [table]: ${properTable.id}")
                            jsonDir + jsonBase + "-Table-" + properTable.id + "-wft-err.json"
                          } else {
                            logger.debug (s"Created wft confidently")
                            jsonDir + jsonBase + "-Table-" + properTable.id + "-wft.json"
                          }
                        scala.tools.nsc.io.File (jsonLoc).writeAll (JSONFormatter.wftToJsonString (wft) )
                        println(s"wft written at $jsonLoc")

                      case _ => logger.warn (s"Could not convert given table to a well formed table. [pdf]:$pdfLoc [table]:${properTable.id}")
                    }

                  case None => logger.warn (s"Table did not have any text. [pdf]: $pdfLoc")
                }
            }
          case  _ => logger.warn(s"allenAI Failed")
        }
      case _ => logger.warn(s"Failed to create simple document through PDSimplify")
    }

  }

  def getListOfFiles(dir: String):List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).toList
    } else {
      List[File]()
    }
  }

  def main(args: Array[String]) {

    val baseDir = "/media/sagnik/OS_Install/data/nlp-table-data/"
    val pdfDir = baseDir + "pdfs/"
    val jsonDir = baseDir + "tablejsons/"
    //val pdfBase = "2"

    val pdfBases = getListOfFiles(pdfDir).map(_.getPath.split("/").last.dropRight(4))

    import scala.language.postfixOps
    for (pdfBase<-pdfBases) {
      println(s"[working on]: $pdfBase")
      logger.debug(s"\n--------------------\n[working on]: $pdfBase\n--------------------\n")
      val pdfLoc = pdfDir + pdfBase + ".pdf"
      HeaderPaths.apply(pdfLoc, jsonDir, pdfBase)
    }
  }

}
