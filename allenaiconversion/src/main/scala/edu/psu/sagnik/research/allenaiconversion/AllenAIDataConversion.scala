package edu.psu.sagnik.research.allenaiconversion

import java.awt.geom.Point2D
import java.io.File

import edu.psu.sagnik.research.pdsimplify.impl.ProcessDocument
import edu.psu.sagnik.research.pdsimplify.model.PDPageSimple
import edu.psu.sagnik.research.pdsimplify.path.impl.BB
import edu.psu.sagnik.research.pdsimplify.path.model.{PDCurve, PDLine, PDSegment, PathStyle}
import edu.psu.sagnik.research.pdsimplify.raster.model.PDRasterImage
import org.allenai.common.Logging
import org.allenai.pdffigures2.{Box, Figure, FigureExtractor, FigureType}
import org.apache.pdfbox.pdmodel.PDDocument

import scala.util.{Failure, Success, Try}

object AllenAIDataConversion extends Logging {

  protected implicit class FloatEquality(val a: Float) extends AnyVal {
    @inline def isEqualFloat(b: Float): Boolean =
      math.abs(a - b) <= 0.00001f

    @inline def isRasterEqualFloat(b: Float): Boolean =
      math.abs(a - b) <= 1f
  }


  type A = TextGeneric

  def A(x: String, y: Rectangle, z: Int) = TextGeneric(x, y, z)

  def jsonToString(inpFile: String): String = scala.io.Source.fromFile(inpFile).mkString

  import org.json4s._
  import org.json4s.jackson.JsonMethods._

  implicit val formats = org.json4s.DefaultFormats
  def jsonToCaseClasses(jsonStr: String): AllenAITable = parse(jsonStr).extract[AllenAITable] //for test

  lazy val tableFromPDFFigures2= (document: FigureExtractor.DocumentWithRasterizedFigures) => {
    document.figures.map(_.figure).filter(_.figType == FigureType.Table).map(t =>
      AllenAITable(
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

  lazy val figureFromPDFFigures2 = (document: FigureExtractor.DocumentWithRasterizedFigures) => {
    document.figures.map(_.figure).filter(_.figType == FigureType.Figure).map(t =>
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

  def isStraightLine(s: PDSegment) = s match {
    case s: PDLine => (s.startPoint.x isEqualFloat s.endPoint.x) || (s.startPoint.y isEqualFloat s.endPoint.y) //a horizontal or a vertical line
    case s: PDCurve =>
      (s.startPoint.x isEqualFloat s.controlPoint1.x) && (s.controlPoint1.x isEqualFloat s.controlPoint2.x) && (s.controlPoint2.x isEqualFloat s.endPoint.x) ||
        (s.startPoint.y isEqualFloat s.controlPoint1.y) && (s.controlPoint1.y isEqualFloat s.controlPoint2.y) && (s.controlPoint2.y isEqualFloat s.endPoint.y)
    //A Beizer curve can be a straight line when the start, end and the control points are in the same line.
    // Among them, we only want the ones that are horizontal or vertical.
    case _ => false
  }

  def isStraightLine(im: PDRasterImage) = (im.bb.x1 isRasterEqualFloat im.bb.x2) || (im.bb.y1 isRasterEqualFloat im.bb.y2) //TODO: a table can have a line
  // that looks like a `/`. To be included later.



  def isWithinFigureTable[B](pdObject: B, figureTableBBVals: Seq[Float], pageHeight: Float) = {
    val maxWidth= 10000f
    val maxHeight = 10000f
    val objectBB = pdObject match {
      case pdObject: PDSegment =>
        Rectangle(
          pdObject.bb.x1,
          pageHeight - pdObject.bb.y1,
          pdObject.bb.x2,
          pageHeight - pdObject.bb.y2
        )
      case pdObject: PDRasterImage =>
        Rectangle(
          pdObject.bb.x1,
          pageHeight - pdObject.bb.y1,
          pdObject.bb.x2,
          pageHeight - pdObject.bb.y2
        )
      case _ => Rectangle(0f, 0f, maxWidth, maxHeight)
    }
    val figureTableBB = Rectangle(
      figureTableBBVals.head,
      figureTableBBVals(1),
      figureTableBBVals(2),
      figureTableBBVals(3)
    )
    //println(s"path BB: ${segmentBB}, table BB: ${tableBB} is inside ${Rectangle.rectInside(segmentBB,tableBB)}")
    Rectangle.rectInside(objectBB, figureTableBB)
  }

  def transformPDSegment(pdSegment: PDSegment, bb: Seq[Float], pageHeight: Float): PDSegment = pdSegment match {
    case pdSegment: PDLine =>
      val startPoint = new Point2D.Float(pdSegment.startPoint.x - bb.head, pageHeight - pdSegment.startPoint.y - bb(1))
      val endPoint = new Point2D.Float(pdSegment.endPoint.x - bb.head, pageHeight - pdSegment.endPoint.y - bb(1))
      PDLine(
        startPoint = startPoint,
        endPoint = endPoint,
        bb = BB.Line(startPoint, endPoint)
      )
    case pdSegment: PDCurve =>
      val startPoint = new Point2D.Float(pdSegment.startPoint.x - bb.head, pageHeight - pdSegment.startPoint.y - bb(1))
      val controlPoint1 = new Point2D.Float(pdSegment.controlPoint1.x - bb.head, pageHeight - pdSegment.controlPoint1.y - bb(1))
      val controlPoint2 = new Point2D.Float(pdSegment.controlPoint2.x - bb.head, pageHeight - pdSegment.controlPoint2.y - bb(1))
      val endPoint = new Point2D.Float(pdSegment.endPoint.x - bb.head, pageHeight - pdSegment.endPoint.y - bb(1))
      PDCurve(
        startPoint=startPoint,
        endPoint=endPoint,
        controlPoint1=controlPoint1,
        controlPoint2=controlPoint2,
        bb = BB.Curve(startPoint,endPoint,controlPoint1,controlPoint2)
      )

  }

  def pDLinefromPoints(sP: Point2D.Float, eP: Point2D.Float, bb: Seq[Float], pageHeight: Float): PDSegment = {
    val startPoint = new Point2D.Float(sP.x - bb.head, pageHeight - sP.y - bb(1))
    val endPoint = new Point2D.Float(eP.x - bb.head, pageHeight - eP.y - bb(1))
    PDLine(
      startPoint = startPoint,
      endPoint = endPoint,
      bb = BB.Line(startPoint, endPoint)
    )
  }

  def rasterToPDLine(im: PDRasterImage, bb: Seq[Float], pageHeight: Float) = List(
    pDLinefromPoints(sP = new Point2D.Float(im.bb.x1, im.bb.y1), eP = new Point2D.Float(im.bb.x1, im.bb.y2), bb = bb, pageHeight = pageHeight),
    pDLinefromPoints(sP = new Point2D.Float(im.bb.x1, im.bb.y2), eP = new Point2D.Float(im.bb.x2, im.bb.y2), bb = bb, pageHeight = pageHeight),
    pDLinefromPoints(sP = new Point2D.Float(im.bb.x2, im.bb.y2), eP = new Point2D.Float(im.bb.x2, im.bb.y1), bb = bb, pageHeight = pageHeight),
    pDLinefromPoints(sP = new Point2D.Float(im.bb.x2, im.bb.y1), eP = new Point2D.Float(im.bb.x1, im.bb.y1), bb = bb, pageHeight = pageHeight)
  )

  def getPDLinesTable(smp: Option[PDPageSimple], bb: Seq[Float], pageNumber: Int) = smp match {

    //println(s"[straight segments]: ${simplePage.gPaths.flatMap(_.subPaths).flatMap(_.segments).count(isStraightLine(_))}")
    case Some(simplePage) =>
      val (pageHeight, pageWidth) = (simplePage.bb.y2 - simplePage.bb.y1, simplePage.bb.x2 - simplePage.bb.x1)
      val pdSegments =
        (for {
          paths <- simplePage.gPaths
          subPaths <- paths.subPaths
          segments <- subPaths.segments
          if isStraightLine(segments) && isWithinFigureTable[PDSegment](segments, bb, pageHeight)
        } yield transformPDSegment(segments, bb, pageHeight)) ++ (for {
          raster <- simplePage.rasters
          if isStraightLine(raster) && isWithinFigureTable[PDRasterImage](raster, bb, pageHeight)
        } yield rasterToPDLine(raster, bb, pageHeight)).flatten
      Some(pdSegments)
    case _ => None

  }

  def getPDPathsFigure(smp: Option[PDPageSimple], bb: Seq[Float], pageNumber: Int): Seq[(PDSegment,PathStyle)] = smp match {

    case Some(simplePage) =>
      val (pageHeight, pageWidth) = (simplePage.bb.y2 - simplePage.bb.y1, simplePage.bb.x2 - simplePage.bb.x1)
      for {
        paths <- simplePage.gPaths
        subPaths <- paths.subPaths
        segments <- subPaths.segments
        if paths.doPaint && isWithinFigureTable[PDSegment](segments, bb, pageHeight)
      } yield (transformPDSegment(segments, bb, pageHeight),paths.pathStyle)

    case _ => Seq.empty[(PDSegment,PathStyle)]

  }


  def getPDRastersFigure(smp: Option[PDPageSimple], bb: Seq[Float], pageNumber: Int): Seq[PDRasterImage] = smp match {
    case Some(simplePage) =>
      val (pageHeight, pageWidth) = (simplePage.bb.y2 - simplePage.bb.y1, simplePage.bb.x2 - simplePage.bb.x1)
      simplePage.rasters.filter(raster => isWithinFigureTable[PDRasterImage](raster, bb, pageHeight))
    case _ => Seq.empty[PDRasterImage]

  }

  def getPageHeightWidth(smp: Option[PDPageSimple], pageNumber: Int) = smp match {
    case Some(simplePage) => Some((simplePage.bb.y2 - simplePage.bb.y1, simplePage.bb.x2 - simplePage.bb.x1))
    case _ => None
  }

  @inline def allenAIBoxtoSeq(b: Box, cvRatio: Float = 1f): Seq[Float] =
    Seq(b.x1.toFloat / cvRatio, b.y1.toFloat / cvRatio, b.x2.toFloat / cvRatio, b.y2.toFloat / cvRatio)

  def allenAITableToMyTable(aTable: AllenAITable, pdfLoc: String): Option[IntermediateTable]= {
    val pdDoc = PDDocument.load(new File(pdfLoc))
    val simpleDocument = Try(ProcessDocument(pdDoc)) match {
      case Success(document) => Some(document);
      case Failure(e) => {
        System.err.println(s"[PDSimplify failed]: ${e.getMessage}")
        None
      }
    }
    pdDoc.close()
    simpleDocument match {
      case Some(doc) => allenAITableToMyTable(aTable, Some(doc.pages(aTable.Page)))
      case _ => None
    }
  }

  def allenAITableToMyTable(aTable: AllenAITable, simplePage: Option[PDPageSimple]): Option[IntermediateTable] = aTable.ImageText match {
    case Some(wordsOrg) =>

      val cvRatio = aTable.DPI / 72f
      val tableBB = allenAIBoxtoSeq(aTable.ImageBB)
      val words = wordsOrg.map(x => x.copy(TextBB = x.TextBB.map(_ / cvRatio)))
      val (pageHeight, pageWidth) =
        getPageHeightWidth(simplePage, aTable.Page) match { case Some((h, w)) => (h, w); case _ => (842f, 595f) } //defaulting to A4
    val imTable = IntermediateTable(
        bb = Rectangle(tableBB.head, tableBB(1), tableBB(2), tableBB(3)),
        textSegments = words.map(w =>
          A(
            w.Text,
            Rectangle(
              w.TextBB.head - tableBB.head + 2, //shortening the table
              w.TextBB(1) - tableBB(1) + 2,
              w.TextBB(2) - tableBB.head - 2,
              w.TextBB(3) - tableBB(1) - 2
            ),
            w.Rotation
          )
        ),

        caption = Some(aTable.Caption),
        mention = aTable.Mention,
        pageNo = aTable.Page,
        pdLines = getPDLinesTable(simplePage, tableBB, aTable.Page) match { case Some(pdLines) => pdLines; case _ => Seq.empty[PDSegment] },
        pageHeight = pageHeight,
        pageWidth = pageWidth,
        dpi = aTable.DPI,
        id = aTable.id
      )
      //println(imTable.pdLines)
      if (imTable.textSegments.nonEmpty) Some(imTable)
      else None

    case _ => None
  }

  def allenAIFigureToMyFigure(aFigure: AllenAIFigure, simplePage: Option[PDPageSimple]): Option[CiteSeerXFigure] = {

    val cvRatio = aFigure.DPI / 72f
    val figureBB = allenAIBoxtoSeq(aFigure.ImageBB)
    val words = aFigure.ImageText.getOrElse(Seq.empty[AllenAIWord])
    val (pageHeight, pageWidth) = getPageHeightWidth(simplePage, aFigure.Page) match {
      case Some((h, w)) => (h, w);
      case _ => (842f, 595f)
    } //defaulting to A4
    val csxFigure = CiteSeerXFigure(
        bb = Rectangle(figureBB.head, figureBB(1), figureBB(2), figureBB(3)),
        words.map(w =>
          A(
            w.Text,
            Rectangle(
              w.TextBB.head - figureBB.head + 2, //shortening the table
              w.TextBB(1) - figureBB(1) + 2,
              w.TextBB(2) - figureBB.head - 2,
              w.TextBB(3) - figureBB(1) - 2
            ),
            w.Rotation
          )
        ),

        caption = Some(aFigure.Caption),
        mention = aFigure.Mention,
        pageNo = aFigure.Page,
        pdSegments = getPDPathsFigure(simplePage, figureBB, aFigure.Page),
        pdRasters= getPDRastersFigure(simplePage, figureBB, aFigure.Page),
        pageHeight = pageHeight,
        pageWidth = pageWidth,
        dpi = aFigure.DPI,
        id = aFigure.id
      )
    //println(imTable.pdLines)
    if (csxFigure.pdSegments.nonEmpty || csxFigure.pdRasters.nonEmpty) Some(csxFigure)
    else None

  }
}
