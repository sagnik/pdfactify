package edu.psu.sagnik.research.table.impl

import edu.psu.sagnik.research.table.model.AllenAIDataConversion
import edu.psu.sagnik.research.table.tablecellextraction.{CellRenaming, CombineWords}
import edu.psu.sagnik.research.table.tripleextraction.TabletoWFT
import java.util.logging.{Level, Logger}

/**
  * Created by schoudhury on 7/25/16.
  */
object HeaderPaths {

  lazy val logger = Logger.getLogger("table.impl.HeaderPaths")

  def apply(pdfLoc:String,jsonDir:String,jsonBase:String)= {
    val myTables = AllenAIDataConversion.fromPDFFigures2(pdfLoc)
      .map(
        atable => AllenAIDataConversion.allenAITableToMyTable(atable, pdfLoc)
      )

    myTables.foreach {
      myTable => myTable match {
        case Some(properTable) =>
          val interimTable = CombineWords.wordMergedTable(properTable)
          val table = CellRenaming.produceRowColNumbers(interimTable)
          TabletoWFT.headerPathstoDataCells(table) match {
            case Some(wft) =>
              val jsonLoc=jsonDir+jsonBase+"-Table-"+properTable.id+"-wft.json"
              if (table.cells.length != interimTable.textSegments.length)
                logger.warning(s"Interim table and Cell Renamed Tables didn't match. " +
                  s"Created $jsonLoc with high Error Probability. [pdf]: $pdfLoc [table]: ${properTable.id}")
              else
                logger.fine(s"Created well formatted table json at $jsonLoc")
              scala.tools.nsc.io.File(jsonLoc).writeAll(JSONFormatter.wftToJsonString(wft))

            case _ => logger.warning(s"Could not convert given table to a well formed table. [pdf]:$pdfLoc [table]: ${properTable.id}")
          }


        case None => logger.warning(s"Table did not have any text. [pdf]: $pdfLoc")
      }
    }
  }

  def main(args: Array[String]) {

    val baseDir="/Users/schoudhury/data/econpapers/ageconsearch.umn.edu/"
    val pdfDir=baseDir+"pdfs/"
    val jsonDir=baseDir+"tablejsons/"
    val pdfBase="1"

    val pdfLoc=pdfDir+pdfBase+".pdf"
    HeaderPaths.apply(pdfLoc,jsonDir,pdfBase)

  }

}
