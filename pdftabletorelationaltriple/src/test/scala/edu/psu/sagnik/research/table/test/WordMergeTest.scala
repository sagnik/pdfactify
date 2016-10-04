package edu.psu.sagnik.research.table.test

import edu.psu.sagnik.research.allenaiconversion.AllenAIDataConversion
import edu.psu.sagnik.research.table.tablecellextraction.CombineWords
import org.scalatest.FunSpec

/** Created by schoudhury on 8/19/15.
  */

class WordMergeTest extends FunSpec {
  describe("testing if word merging is correct") {
    it("should print the merged cells from a table") {
      val mytable = AllenAIDataConversion.
        allenAITableToMyTable(
          AllenAIDataConversion.jsonToCaseClasses(
            AllenAIDataConversion.jsonToString(DataLocation.jsonLoc)
          ), DataLocation.pdfLoc
        )
      mytable match {
        case Some(propertable) => {
          val interimtable = CombineWords.wordMergedTable(propertable)
          println(s"caption: ${interimtable.caption}")
          println(s"mention: ${interimtable.mention}")
          println(s"bounding box: ${interimtable.bb}")
          interimtable.textSegments.foreach(x => println(s"merged words: ${x.content} bb: ${x.bb}"))
        }
        case None => assert(false)
      }

    }
  }
}
