package edu.psu.sagnik.research.figuretableextractionSVG

/**
  * Created by sagnik on 10/1/16.
  */

import edu.psu.sagnik.research.pdsimplify.path.model._

object PathHelper {
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
        s.endPoint.x.toString + "," + s.endPoint.y.toString

    case _ => ""

  }

  def segmentToString(s: PDSegment, h: Float): String = s match {
    case s: PDLine =>
      "M " +
        s.startPoint.x.toString + "," + (h-s.startPoint.y).toString +
        " L " +
        s.endPoint.x.toString + "," + (h- s.endPoint.y).toString +
        " "
    case s: PDCurve =>
      "M " +
        s.startPoint.x.toString + "," + (h- s.startPoint.y).toString +
        " C " +
        s.controlPoint1.x.toString + "," + (h - s.controlPoint1.y).toString + " " +
        s.controlPoint2.x.toString + "," + (h - s.controlPoint2.y).toString + " " +
        s.endPoint.x.toString + "," + (h - s.endPoint.y).toString

    case _ => ""

  }


  val defaultPathStyle = "style=\"fill:none;" +
    "stroke:#000000;stroke-width:1.00000;" +
    "stroke-linecap:butt;stroke-linejoin:miter;" +
    "stroke-miterlimit:10;stroke-dasharray:none;" +
    "stroke-opacity:1\""

  def getStyleString(pStyle: Option[PathStyle]): String = pStyle match{
    case Some(p) =>
      val styleStart = " style=\""
      val styles = List(
        p.fill match { case Some(f) => "fill:" + f; case _ => "fill:none" },
        p.fillRule match { case Some(f) => "fill-rule:" + f; case _ => "fill-rule:nonzero" },
        p.fillOpacity match { case Some(f) => "fill-opacity:" + f; case _ => "fill-opacity:1" },
        p.stroke match { case Some(f) => "stroke:" + f; case _ => "stroke:none" },
        p.strokeWidth match { case Some(f) => "stroke-width:" + f; case _ => "stroke-width:1" },
        p.strokeLineCap match { case Some(f) => "stroke-linecap:" + f; case _ => "stroke-linecap:butt" },
        p.strokeLineJoin match { case Some(f) => "stroke-linejoin:" + f; case _ => "stroke-linejoin:miter" },
        p.strokeMiterLimit match { case Some(f) => "stroke-miterlimit:" + f; case _ => "stroke-miterlimit:4" },
        p.strokeDashArray match { case Some(f) => "stroke-dasharray:" + f; case _ => "stroke-dasharray:none" },
        p.strokeDashOffset match { case Some(f) => "stroke-dashoffset:" + f; case _ => "stroke-dashoffset:0" },
        p.strokeOpacity match { case Some(f) => "stroke-opacity:" + f; case _ => "stroke-opacity:1" }
      ).mkString(";")
      val styleEnd = "\""
      styleStart + styles + styleEnd
    case _ => defaultPathStyle
  }

  def getPathDString(p: PDPath, h: Float): String = {
    val dStringStart = " d=\""
    val segmentStrings = p.subPaths.flatMap(x => x.segments).foldLeft(" ")((a, b) => a + segmentToString(b, h))
    val dStringEnd = "\""
    dStringStart + segmentStrings + dStringEnd
  }


}
