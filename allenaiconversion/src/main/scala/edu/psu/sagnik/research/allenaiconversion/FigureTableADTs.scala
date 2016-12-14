package edu.psu.sagnik.research.allenaiconversion

import edu.psu.sagnik.research.pdsimplify.path.model.{PDPath, PDSegment, PathStyle}
import edu.psu.sagnik.research.pdsimplify.raster.model.PDRasterImage
import org.allenai.pdffigures2.Box

/**
  * Created by sagnik on 10/1/16.
  */

case class TextGeneric(content: String, bb: Rectangle, rotation: Int)

case class AllenAIWord(Rotation: Int, Text: String, TextBB: Seq[Float])

case class AllenAITableParse(
                         Caption: String,
                         CaptionBB: Seq[Float],
                         Page: Int,
                         ImageBB: Seq[Float],
                         ImageText: Option[Seq[AllenAIWord]],
                         Mention: Option[String],
                         DPI: Int,
                         Number: String
                       )

case class AllenAITable(
                         Caption: String,
                         CaptionBB: Box,
                         Page: Int,
                         ImageBB: Box,
                         ImageText: Option[Seq[AllenAIWord]],
                         Mention: Option[String],
                         DPI: Int,
                         id: String
                       )

//we must have bb and text segments. Others can be skipped
case class IntermediateTable(
                              bb: Rectangle,
                              textSegments: Seq[TextGeneric],
                              caption: Option[String],
                              mention: Option[String],
                              pageNo: Int,
                              pdLines: Seq[PDSegment],
                              pageHeight: Float,
                              pageWidth: Float,
                              dpi: Int,
                              id: String
                            )
case class AllenAIFigure(
                          Caption: String,
                          CaptionBB: Box,
                          Page: Int,
                          ImageBB: Box,
                          ImageText: Option[Seq[AllenAIWord]],
                          Mention: Option[String],
                          DPI: Int,
                          id: String
                        )

case class CiteSeerXFigure(
                            bb: Rectangle,
                            textSegments: Seq[TextGeneric],
                            caption: Option[String],
                            mention: Option[String],
                            pageNo: Int,
                            pdSegments: Seq[(PDSegment,PathStyle)],
                            pdRasters: Seq[PDRasterImage],
                            pageHeight: Float,
                            pageWidth: Float,
                            dpi: Int,
                            id: String
                        )

