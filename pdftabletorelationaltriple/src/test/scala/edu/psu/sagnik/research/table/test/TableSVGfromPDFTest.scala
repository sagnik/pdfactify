package edu.psu.sagnik.research.table.test

import edu.psu.sagnik.research.allenaiconversion.{AllenAIDataConversion, IntermediateTable}
import edu.psu.sagnik.research.pdsimplify.path.model.{PDCurve, PDLine, PDSegment}
import edu.psu.sagnik.research.pdsimplify.text.model.PDChar
import org.scalatest.FunSpec

import scala.reflect.io.File

/** Created by schoudhury on 8/21/15.
  */

class TableSVGfromPDFTest extends FunSpec {

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
      val myTables = Seq.empty[Option[IntermediateTable]]

        /* TODO: change for test
        AllenAIDataConversion.tableFromPDFFigures2(DataLocation.pdfLoc)
        .map(
          atable => AllenAIDataConversion.allenAITableToMyTable(atable, DataLocation.pdfLoc)
        )*/

      for (myTable <- myTables) {
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

            val content = pdLines.map(x => getSvgString[PDSegment](x)).foldLeft("")((a, b) => a + "\n" + b) + "\n"

            val svgEnd = "\n</svg>"

            val svgLoc = DataLocation.baseSVGDir + DataLocation.baseFile + "-Table-" + properTable.id + ".svg"
            File(svgLoc).writeAll(svgStart + content + svgEnd)
            println(s"written svg file at ${svgLoc} [caption]: ${properTable.caption} [words]: ${properTable.textSegments.map(_.content)} [bb]: ${properTable.bb}")

          case _ => assert(false)
        }

      }
    }
  }
}
