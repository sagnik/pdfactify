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

  def RowHeaderPathTest(jsonLoc: String): Unit = {
    val mytable = AllenAIDataConversion.
      allenAITableToMyTable(
        AllenAIDataConversion.jsonTocaseClasses(
          AllenAIDataConversion.jsonToString(jsonLoc)
        ), DataLocation.pdfLoc
      )
    implicit val formats = org.json4s.DefaultFormats
    mytable match {
      case Some(propertable) => {
        val interimtable = CombineWords.wordMergedTable(propertable)
        val table = CellRenaming.produceRowColNumbers(interimtable)
        if (table.cells.length != interimtable.textSegments.length)
          println("progressing with possible errors")
        TabletoWFT.headerPathstoDataCells(table) match {
          case Some(wft) => {
            scala.tools.nsc.io.File(jsonLoc.substring(0, jsonLoc.length - 5) + "-wft.json")
              .writeAll(JSONFormatter.wftToJsonString(wft))
          }
          case None => println("Could not convert given table to a well formed table")
        }
      }
      case None => println("Failed to generate AllenAI table")
    }

  }
  describe("testing if row column prediction is correct") {
    it("should print the rows and cols from a table") {
      //first delete all existing wft jsons.
      DataLocation.recursiveListFiles(new File("/home/sagnik/data/nlp-table-data/randjsons/"), "(?=.*Table)(?=.*wft)(?=.*json)".r)
        .foreach(x => deleteFile(x.getAbsolutePath))

      DataLocation.recursiveListFiles(new File("/home/sagnik/data/nlp-table-data/randjsons/"), "(?=.*Table)(?=.*json)".r)
        .filterNot(x => blackList.exists(y => x.getAbsolutePath.contains(y)))
        .foreach(x => { println(x.getAbsolutePath); RowHeaderPathTest(x.getAbsolutePath) })
    }
  }
}
