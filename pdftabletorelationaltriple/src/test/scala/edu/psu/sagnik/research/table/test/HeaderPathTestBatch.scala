package edu.psu.sagnik.research.table.test

import java.io.File

import edu.psu.sagnik.research.allenaiconversion.AllenAIDataConversion
import edu.psu.sagnik.research.table.impl.JSONFormatter
import edu.psu.sagnik.research.table.tablecellextraction.{CellRenaming, CombineWords}
import edu.psu.sagnik.research.table.tripleextraction.TabletoWFT
import org.scalatest.FunSpec

/** Created by schoudhury on 8/19/15.
  */

class HeaderPathTestBatch extends FunSpec {

  val blackList = List("P10-1143-Table-2.json", "E03-1023-Table-1.json", "J05-4002-Table-1.json", "P06-1100-Table-3.json")

  def deleteFile(path: String) = {
    val fileTemp = new File(path)
    if (fileTemp.exists) {
      fileTemp.delete()
    }
  }

  def RowHeaderPathTest(jsonLoc: String, pdfLoc:String): Unit = {
    val mytable = AllenAIDataConversion.
      allenAITableToMyTable(
        AllenAIDataConversion.jsonToCaseClasses(
          AllenAIDataConversion.jsonToString(jsonLoc)
        ), pdfLoc
      )
    mytable match {
      case Some(properTable) => {
        val interimTable = CombineWords.wordMergedTable(properTable)
        val table = CellRenaming.produceRowColNumbers(interimTable)
        //table.cells.foreach{x=>println(x.tg.content,x.startRow,x.startCol)}
        val wftJsonLoc =
          if (table.cells.length != interimTable.textSegments.length)
          //println(s"high error probability: ${table.cells.length}, ${interimtable.textSegments.length}")
            jsonLoc.substring(0, jsonLoc.length - 5) + "-wft-err.json"
          else
            jsonLoc.substring(0, jsonLoc.length - 5) + "-wft.json"

        TabletoWFT.headerPathstoDataCells(table) match {
          case Some(wft) => scala.tools.nsc.io.File(wftJsonLoc).writeAll(JSONFormatter.wftToJsonString(wft))

          case None => println("Could not convert given table to a well formed table")
        }
      }
      case None => println(s"could not get table for $jsonLoc")
    }
  }

  describe("testing if row column prediction is correct") {
    it("should print the rows and cols from a table") {
      val baseDir="/media/sagnik/OS_Install/data/nlp-table-data"
      val basePDFDir = s"$baseDir/pdfs"

      //first delete all existing wft jsons.
      DataLocation.recursiveListFiles(new File(s"$baseDir/tablejsons/"), "(?=.*Table)(?=.*wft)(?=.*json)".r)
        .foreach(x => deleteFile(x.getAbsolutePath))

      DataLocation.recursiveListFiles(new File(s"$baseDir/tablejsons/"), "(?=.*Table)(?=.*json)".r)
        .filterNot(x => blackList.exists(y => x.getAbsolutePath.contains(y)))
        .foreach(x => {
          val jsonLoc = x.getAbsolutePath
          println(jsonLoc)
          val pdBase =  jsonLoc.split("/").last.split("-Table").head
          val pdLoc = s"$basePDFDir/$pdBase.pdf"
          RowHeaderPathTest(x.getAbsolutePath,pdLoc)
        }
        )
    }
  }
}
