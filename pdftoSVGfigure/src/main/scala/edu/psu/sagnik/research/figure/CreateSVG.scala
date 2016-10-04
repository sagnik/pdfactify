package edu.psu.sagnik.research.figure

import java.io.File
import java.nio.file.{Files, Paths}
import java.util.logging.{Level, Logger}

import edu.psu.sagnik.research.allenaiconversion.AllenAIDataConversion
import edu.psu.sagnik.research.pdsimplify.impl.ProcessDocument
import edu.psu.sagnik.research.pdsimplify.path.model._
import edu.psu.sagnik.research.pdsimplify.text.model.PDChar
import org.allenai.common.Logging
import org.allenai.pdffigures2.{Box, FigureExtractor, FigureType}
import org.apache.pdfbox.pdmodel.PDDocument

import scala.util.{Failure, Success, Try}


object CreateSVG extends App with Logging{

  @inline def allenAIBoxtoSeq(b: Box, cvRatio: Float = 1f): Seq[Float] =
    Seq(b.x1.toFloat / cvRatio, b.y1.toFloat / cvRatio, b.x2.toFloat / cvRatio, b.y2.toFloat / cvRatio)

  val pdLoc="/home/sagnik/data/citeseer10000withsvg/10.1.1.67.2476.pdf"

  val allenAIFigures=AllenAIDataConversion.figureFromPDFFigures2(pdLoc)
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
      val csxFigures = allenAIFigures.map {
        x => AllenAIDataConversion.allenAIFigureToMyFigure(x,Some(doc.pages(x.Page)))
      }
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
          new createSVG[PDSegment] ()
            .writeSVG (
              sequence = f.pdLines.toList,
              svgLoc = s"${svgDir.getAbsolutePath}/${pdLoc.split ("/").last.dropRight (4)}${f.id}.svg",
              width = f.bb.x2 - f.bb.x1,
              height = f.bb.y2 - f.bb.x1
            )
        )
      }
    case _ => {s"Could not create SVGs for ${pdLoc}"}
  }
}

class createSVG[A] extends Logging{

  def getSvgString(p: A, w: Float, h: Float): String = p match {
    case p: PDPath => "<path " + PathHelper.getPathDString(p, h) + " " + PathHelper.getStyleString(p.pathStyle) + " />"
    case _ => ""
  }

  def writeSVG(sequence: List[A], svgLoc: String, width: Float, height: Float): Unit = {
    val svgStart = "<?xml version=\"1.0\" standalone=\"no\"?>\n\n<svg height=\"" +
      height +
      "\" width=\"" +
      width +
      "\" version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\">" +
      "\n"

    val first = sequence.headOption
    first match {
      case Some(first) =>
        val content = first match {
          case first: PDPath => sequence.map(x => getSvgString(x, width, height)).mkString("\n")
          //case first: PDChar => sequence.map(x => getSvgString(x, width, height)).foldLeft("")((a, b) => a + "\n" + b) + "\n"
          case _ => logger.info(s"${first.getClass}"); ???

        }
        val svgEnd = "\n</svg>"
        import scala.reflect.io.File
        File(svgLoc).writeAll(svgStart + content + svgEnd)
        println(s"written SVG at ${svgLoc}")
      case _ => {}
    }
  }
}

