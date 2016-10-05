package edu.psu.sagnik.research.figure

import java.io.File
import java.nio.file.{Files, Paths}
import java.util.logging.{Level, Logger}

import edu.psu.sagnik.research.allenaiconversion.AllenAIDataConversion
import edu.psu.sagnik.research.pdsimplify.impl.ProcessDocument
import edu.psu.sagnik.research.pdsimplify.path.model._
import edu.psu.sagnik.research.pdsimplify.raster.model.PDRasterImage
import edu.psu.sagnik.research.pdsimplify.text.model.PDChar
import org.allenai.common.Logging
import org.allenai.pdffigures2.{Box, FigureExtractor, FigureType}
import org.apache.pdfbox.pdmodel.PDDocument

import scala.util.{Failure, Success, Try}


object CreateSVG extends App with Logging{

  @inline def allenAIBoxtoSeq(b: Box, cvRatio: Float = 1f): Seq[Float] =
    Seq(b.x1.toFloat / cvRatio, b.y1.toFloat / cvRatio, b.x2.toFloat / cvRatio, b.y2.toFloat / cvRatio)

  //val pdLoc="/home/sagnik/data/citeseer10000withsvg/10.1.1.67.2476.pdf"
  val pdLoc="/home/sagnik/Downloads/ketwww15.pdf"

  val allenAIFigures=AllenAIDataConversion.figureFromPDFFigures2(pdLoc)
  val allenAITables=AllenAIDataConversion.tableFromPDFFigures2(pdLoc)

  println(s"${allenAIFigures.size} figures created from AllenAI")

  val pdDoc = PDDocument.load(new File(pdLoc))
  val simpleDocument = Try(ProcessDocument(pdDoc)) match {
    case Success(document) => Some(document);
    case Failure(e) => {
      System.err.println(s"[PDSimplify failed]: ${e.getMessage}")
      None
    }
  }
  pdDoc.close()

  simpleDocument match {
    case Some(doc) =>
      val csxFigures = allenAIFigures.map {x => AllenAIDataConversion.allenAIFigureToMyFigure( x,Some(doc.pages(x.Page)))}
      val csxTables = allenAITables.map {x => AllenAIDataConversion.allenAITableToMyTable( x,Some(doc.pages(x.Page)))}
      println (s"${csxFigures.size} figures created for CiteSeerX")
      val svgDir = new File (pdLoc.dropRight (4) )
      import org.apache.commons.io.FileUtils

      val dirResult = if (! svgDir.exists) svgDir.mkdir
      else {
        FileUtils.deleteDirectory (svgDir)
        svgDir.mkdir
      }
      if (dirResult) {
        csxFigures.flatten.foreach (f =>
          new createSVG()
            .writeSVG (
              paths = f.pdSegments,
              rasters = f.pdRasters,
              svgLoc = s"${svgDir.getAbsolutePath}/${pdLoc.split ("/").last.dropRight (4)}-Figure-${f.id}.svg",
              width = f.bb.x2 - f.bb.x1,
              height = f.bb.y2 - f.bb.y1
            )
        )
        csxTables.flatten.foreach (f =>
          new createSVG()
            .writeSVG (
              paths = f.pdLines,
              svgLoc = s"${svgDir.getAbsolutePath}/${pdLoc.split ("/").last.dropRight (4)}-Table-${f.id}.svg",
              width = f.bb.x2 - f.bb.x1,
              height = f.bb.y2 - f.bb.y1
            )
        )

      }
    case _ => {s"Could not create SVGs for ${pdLoc}"}
  }
}

class createSVG extends Logging{

  def getSvgString[A](p: A, pathStyle: Option[PathStyle], w: Float, h: Float): String = p match {

    case p:PDSegment =>
      "<path d=\"" +
        PathHelper.segmentToString (p) + "\" " + PathHelper.getStyleString (pathStyle) +
        " />"

    case p:PDRasterImage => "<image width=\"" +
      (p.bb.x2-p.bb.x1) +
      "\" height=\"" +
      (p.bb.y2-p.bb.y1) +
      "\" xlink:href=\"data:image/png;base64," +
      p.imageDataString +
      "\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" />"

    case _ => ""
  }

  def writeSVG(paths: Seq[PDSegment],svgLoc: String, width: Float, height: Float): Unit = {
    val svgStart = "<?xml version=\"1.0\" standalone=\"no\"?>\n\n<svg height=\"" +
      height +
      "\" width=\"" +
      width +
      "\" version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\">" +
      "\n"

    val content = paths.map(x => getSvgString(x, None,width, height)).mkString("\n")
    val svgEnd = "\n</svg>"
    import scala.reflect.io.File
    File(svgLoc).writeAll(svgStart + content + svgEnd)
    println(s"written SVG at ${svgLoc}")
  }

  def writeSVG(
                paths: Seq[(PDSegment,PathStyle)],
                rasters: Seq[PDRasterImage],
                svgLoc: String, width: Float, height: Float
              ): Unit = {
    val svgStart = "<?xml version=\"1.0\" standalone=\"no\"?>\n\n<svg height=\"" +
      height +
      "\" width=\"" +
      width +
      "\" version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\">" +
      "\n"

    val content = paths.map(x => getSvgString(x._1, Some(x._2),width, height)).mkString("\n") +
      rasters.map(x => getSvgString(x, None, width, height)).mkString("\n")
    val svgEnd = "\n</svg>"
    import scala.reflect.io.File
    File(svgLoc).writeAll(svgStart + content + svgEnd)
    println(s"written SVG at ${svgLoc}")
  }
}


