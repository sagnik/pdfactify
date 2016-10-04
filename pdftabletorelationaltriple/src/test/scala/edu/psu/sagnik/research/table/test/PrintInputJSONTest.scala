package edu.psu.sagnik.research.table.test

import edu.psu.sagnik.research.allenaiconversion.AllenAIDataConversion
import org.scalatest.FunSpec

/** Created by schoudhury on 8/6/15.
  */
class PrintInputJSONTest extends FunSpec {
  describe("testing whether the input has been parsed properly") {
    it("should print the words from a table.") {
      val words = AllenAIDataConversion.jsonToCaseClasses(AllenAIDataConversion.jsonToString(DataLocation.jsonLoc)).ImageText
      words match {
        case Some(words) => words.foreach(w => println(s"word: ${w.Text} bb: ${w.TextBB}"))
        case None => println { "no words in table" }
      }

    }
  }
}

