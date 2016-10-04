package edu.psu.sagnik.research.table.test

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

import edu.psu.sagnik.research.allenaiconversion.{AllenAIDataConversion, Rectangle}
import edu.psu.sagnik.research.table.tablecellextraction.CombineWords
import org.scalatest.FunSpec

/** Created by schoudhury on 8/19/15.
  */

class WordMergeTestDraw extends FunSpec {

  def createImage(jsonLoc: String, imageLoc: String): Unit = {
    val myTable = AllenAIDataConversion.
      allenAITableToMyTable(
        AllenAIDataConversion.jsonToCaseClasses(
          AllenAIDataConversion.jsonToString(jsonLoc)
        ), DataLocation.pdfLoc
      )
    myTable match {
      case Some(properTable) => {
        val cvRatio = properTable.dpi / 72f
        val interimTable = CombineWords.wordMergedTable(properTable)
        //drawing image
        val sourceImage = new File(imageLoc);
        val original = ImageIO.read(sourceImage);
        val newimage = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_RGB);
        val graph = newimage.createGraphics()
        graph.drawImage(original, 0, 0, null)
        graph.setColor(Color.GREEN)
        interimTable.textSegments
          .map(_.bb)
          .map(x => Rectangle(x.x1 * cvRatio, x.y1 * cvRatio, x.x2 * cvRatio, x.y2 * cvRatio))
          .foreach(x =>
            graph.draw(
              new java.awt.Rectangle(
                x.x1.toInt, x.y1.toInt, (x.x2 - x.x1).toInt, (x.y2 - x.y1).toInt
              )
            ))
        graph.dispose()
        ImageIO.write(newimage, "png", new File(imageLoc.substring(0, imageLoc.length - 4) + "-wordmerged.png"))
        println(s"[word merged image created at]: ${imageLoc.substring(0, imageLoc.length - 4) + "-wordmerged.png"}")
      }
      case None => { println("could not merge words in the table"); return }
    }

  }

  describe("testing if word merging is correct") {
    it("should print the merged cells from a table") {
      //val alljsons=DataLocation.recursiveListFiles(new File("/Users/schoudhury/com-sc-papers/nlp-data/"),"(?=.*Table)(?=.*json)".r)
      //println(alljsons.length)
      //alljsons.foreach(x=>{println(x.getAbsolutePath);createImage(x.getAbsolutePath)})
      val jsonLoc = DataLocation.jsonLoc
      val imageLoc = DataLocation.imageLoc
      createImage(jsonLoc, imageLoc)

    }
  }
}
