package edu.psu.sagnik.research.table.test

import edu.psu.sagnik.research.allenaiconversion.{AllenAIDataConversion, Rectangle}
import edu.psu.sagnik.research.pdsimplify.path.model.{PDCurve, PDLine, PDSegment, PathStyle}
import edu.psu.sagnik.research.pdsimplify.text.model.PDChar
import edu.psu.sagnik.research.table.tablecellextraction.{CellRenaming, CombineWords}
import edu.psu.sagnik.research.table.tripleextraction.CriticalCell
import org.scalatest.FunSpec

import scala.reflect.io.File

/** Created by schoudhury on 8/21/15.
  */

class TableSVGTest extends FunSpec {

  def segmentToString(s: PDSegment): String = s match {
    case s: PDLine =>
      "M " +
        s.startPoint.x.toString + "," + s.startPoint.y.toString +
        " L " +
        s.endPoint.x.toString + "," + s.endPoint.y.toString +
        " "
    case s: PDCurve =>
      "M " +
        s.startPoint.x.toString + "," + s.startPoint.y.toString +
        " C " +
        s.controlPoint1.x.toString + "," + s.controlPoint1.y.toString + " " +
        s.controlPoint2.x.toString + "," + s.controlPoint2.y.toString + " " +
        s.endPoint.x.toString + "," + s.endPoint.y.toString +
        " "
    case _ => ""

  }

  val defaultPathStyle = "style=\"fill:none;" +
    "stroke:#000000;stroke-width:1.00000;" +
    "stroke-linecap:butt;stroke-linejoin:miter;" +
    "stroke-miterlimit:10;stroke-dasharray:none;" +
    "stroke-opacity:1\""

  def getPathDString(p: PDSegment): String = {
    val dStringStart = " d=\""
    val segmentStrings = segmentToString(p)
    val dStringEnd = "\""
    dStringStart + segmentStrings + dStringEnd
  }
  def getSvgString[A](p: A): String = p match {
    case p: PDSegment => "<path " + getPathDString(p) + " " + defaultPathStyle + " />"
    case p: PDChar => ???
  }

  describe("creates an SVG for the table with the paths") {
    it("should create an SVG for the table with the paths") {
      val myTable = AllenAIDataConversion.
        allenAITableToMyTable(
          AllenAIDataConversion.jsonToCaseClasses(
            AllenAIDataConversion.jsonToString(DataLocation.jsonLoc)
          ), DataLocation.pdfLoc
        )

      myTable match {
        case Some(properTable) =>
          val pdLines = properTable.pdLines
          //pdLines.foreach(println)
          val svgWidth = properTable.bb.x2 - properTable.bb.x1
          val svgHeight = properTable.bb.y2 - properTable.bb.y1
          val pageHeight = properTable.pageHeight
          val pageWidth = properTable.pageWidth

          val svgStart = "<?xml version=\"1.0\" standalone=\"no\"?>\n\n<svg height=\"" +
            svgHeight.toString +
            "\" width=\"" +
            svgWidth.toString +
            "\" version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\">" +
            "\n"

          val content = pdLines.map(x => getSvgString[PDSegment](x)).mkString("\n")

          val svgEnd = "\n</svg>"
          File(DataLocation.svgLoc).writeAll(svgStart + content + svgEnd)
          println(s"written svg file at ${DataLocation.svgLoc}")

        case _ => assert(false)
      }

    }
  }
}
