package edu.psu.sagnik.research.table.test

import edu.psu.sagnik.research.table.tablecellextraction.{ CellRenaming, CombineWords }
import edu.psu.sagnik.research.table.model.AllenAIDataConversion
import org.scalatest.FunSpec

/** Created by schoudhury on 8/19/15.
  */
class RowColPredictionTest extends FunSpec {
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
          println(s"caption: ${table.caption}")
          println(s"mention: ${table.context}")
          println(s"bounding box: ${table.bb}")
          table.cells.foreach(x => println(s"cell: startrow: ${x.startRow}, startcol: ${x.startCol}" +
            s"content: ${x.tg.content}"))
          assert(table.cells.length == interimtable.textSegments.length)
        }
        case None => assert(false)
      }

    }
  }
}

