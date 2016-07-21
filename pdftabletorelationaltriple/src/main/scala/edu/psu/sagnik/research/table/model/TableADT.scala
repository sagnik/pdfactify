package edu.psu.sagnik.research.table.model

import edu.psu.sagnik.research.pdsimplify.path.model.{ PDLine, PDSegment, PathStyle }

/** Created by schoudhury on 8/13/15.
  */

case class TextGeneric(content: String, bb: Rectangle)
sealed trait Cell {
  def startRow: Int
  def startCol: Int
  def tg: TextGeneric
}

case class UnClassifiedCell(startRow: Int, startCol: Int, tg: TextGeneric) extends Cell
case class RowHeaderCell(startRow: Int, startCol: Int, tg: TextGeneric) extends Cell
case class ColHeaderCell(startRow: Int, startCol: Int, tg: TextGeneric) extends Cell
case class DataCell(startRow: Int, startCol: Int, tg: TextGeneric, rowpath: Seq[RowHeaderCell], colpath: Seq[ColHeaderCell]) extends Cell
//ideally, a data cell should include some other context thing as well, but that's a whole lot of relation extraction research!

case class AllenAIWord(Rotation: Int, Text: String, TextBB: Seq[Float])

case class AllenAITable(Caption: Option[String], CaptionBB: Option[Seq[Int]], Page: Int,
  ImageBB: Seq[Int], ImageText: Option[Seq[AllenAIWord]],
  Mention: Option[String], DPI: Int, Height: Int, Width: Int)

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
  dpi: Int
)

case class Table(
  pageNumber: Int, //page number
  bb: Rectangle,
  content: String,
  cells: Seq[UnClassifiedCell],
  caption: Option[String], //if we have captions
  context: Option[String] //if we get mentions somewhere in the text
// such as "in Table 5., we show the SVM v/s CRF",
)
//WFT->Well Formed Table, https://cs.uwaterloo.ca/research/tr/1996/09/CS-96-09.pdf
case class WFT(
  pageNumber: Int,
  bb: Rectangle,
  content: String,
  caption: Option[String],
  context: Option[String],
  rhcs: Seq[RowHeaderCell],
  chcs: Seq[ColHeaderCell],
  dcs: Seq[DataCell]
)

