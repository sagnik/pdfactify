package edu.psu.sagnik.research.figure

import java.io.File
import java.util.logging.{Level, Logger}

import edu.psu.sagnik.research.pdsimplify.impl.ProcessDocument
import edu.psu.sagnik.research.pdsimplify.path.model._
import edu.psu.sagnik.research.pdsimplify.text.model.PDChar
import edu.psu.sagnik.research.figure.AllenAIFigure
import org.allenai.common.Logging
import org.allenai.pdffigures2.{Box, FigureExtractor, FigureType}
import org.apache.pdfbox.pdmodel.PDDocument

import scala.util.{Failure, Success, Try}


object CreateSVG extends App with Logging{

  @inline def allenAIBoxtoSeq(b: Box, cvRatio: Float = 1f): Seq[Float] =
    Seq(b.x1.toFloat / cvRatio, b.y1.toFloat / cvRatio, b.x2.toFloat / cvRatio, b.y2.toFloat / cvRatio)

  lazy val fromPDFFigures2 = (pdLoc: String) => {
    val doc = PDDocument.load(new File(pdLoc))
    val figureExtractor = FigureExtractor()
    val document = figureExtractor.getFiguresWithText(doc)
    document.figures.filter(_.figType == FigureType.Figure).map(t =>
      AllenAIFigure(
        Caption = t.caption,
        Page = t.page,
        CaptionBB = t.captionBoundary,
        ImageBB = t.regionBoundary,
        ImageText = if (t.imageText.isEmpty) None else Some(t.imageText.map(x => AllenAIWord(0, x.text, allenAIBoxtoSeq(x.boundary)))), //TODO: rotation
        Mention = None,
        DPI = 72,
        id = t.id
      ))

  }

  val loc="/home/sagnik/data/nlp-table-data/pdfs/Y10-1013.pdf"
}

  class createSVG[A] {
    lazy val logger = Logger.getLogger("pdwriters.writers.svg.CreateSVG")
    logger.setLevel(Level.ALL)

    def getSvgString(p: A, w: Float, h: Float): String = p match {
      case p: PDPath => "<path " + PathHelper.getPathDString(p, h) + " " + PathHelper.getStyleString(p.pathStyle) + " />"
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
            case first: PDPath => sequence.map(x => getSvgString(x, width, height)).foldLeft("")((a, b) => a + "\n" + b) + "\n"
            case first: PDChar => sequence.map(x => getSvgString(x, width, height)).foldLeft("")((a, b) => a + "\n" + b) + "\n"
            case _ => {
              logger.info(s"${first.getClass}"); ???
            }
          }
          val svgEnd = "\n</svg>"
          import scala.reflect.io.File
          File(svgLoc).writeAll(svgStart + content + svgEnd)
        case _ => {}
      }
    }
  }

