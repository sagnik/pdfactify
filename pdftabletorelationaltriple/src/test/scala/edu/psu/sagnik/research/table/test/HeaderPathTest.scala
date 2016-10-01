package edu.psu.sagnik.research.table.test

import edu.psu.sagnik.research.allenaiconversion.AllenAIDataConversion
import edu.psu.sagnik.research.table.impl.JSONFormatter
import edu.psu.sagnik.research.table.tablecellextraction.{CellRenaming, CombineWords}
import edu.psu.sagnik.research.table.tripleextraction.TabletoWFT
import org.scalatest.FunSpec

/** Created by schoudhury on 8/19/15.
  */
class HeaderPathTest extends FunSpec {

  describe("testing if row column prediction is correct") {
    it("should print the rows and cols from a table") {
      val mytable = AllenAIDataConversion.
        allenAITableToMyTable(
          AllenAIDataConversion.jsonTocaseClasses(
            AllenAIDataConversion.jsonToString(DataLocation.jsonLoc)
          ), DataLocation.pdfLoc
        )
      mytable match {
        case Some(propertable) => {
          val interimtable = CombineWords.wordMergedTable(propertable)
          val table = CellRenaming.produceRowColNumbers(interimtable)
          //table.cells.foreach{x=>println(x.tg.content,x.startRow,x.startCol)}

          if (table.cells.length != interimtable.textSegments.length)
            println(s"high error probability: ${table.cells.length}, ${interimtable.textSegments.length}")
          TabletoWFT.headerPathstoDataCells(table) match {
            case Some(wft) => {
              /*scala.tools.nsc.io.File(DataLocation.jsonLoc.split(".json")(0)+"-wft.json")
                .writeAll(JSONFormatter.wftToJsonString(wft))*/

              println("\n-----------------\ncaption\n-----------------\n")
              println(wft.caption)
              println("\n-----------------\nmention\n-----------------\n")
              println(wft.context)
              println("\n-----------------\ntable content\n-----------------\n")
              println(wft.content)
              println("\n-----------------\nrow header cells\n-----------------\n")
              wft.rhcs.foreach(x => println(s"${x.tg.content}"))
              println("\n-----------------\ncol header cells\n-----------------\n")
              wft.chcs.foreach(x => println(s"${x.tg.content}"))
              println("\n-----------------\ndata cells\n-----------------\n")
              wft.dcs.foreach(x => println(s"${x.tg.content} rowpath: ${x.rowpath.map(a => a.tg.content)}" +
                s"colpath: ${x.colpath.map(a => a.tg.content)}"))
              println(DataLocation.jsonLoc.substring(0, DataLocation.jsonLoc.length - 5) + "-wft.json")
              scala.tools.nsc.io.File(DataLocation.jsonLoc.substring(0, DataLocation.jsonLoc.length - 5) + "-wft.json")
                .writeAll(JSONFormatter.wftToJsonString(wft))
            }
            case None => println("Could not convert given table to a well formed table")
          }
        }
        case None => assert(false)
      }

    }
  }
}
