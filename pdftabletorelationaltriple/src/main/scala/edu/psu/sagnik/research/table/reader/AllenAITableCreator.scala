package edu.psu.sagnik.research.table.reader

import java.io.File

import org.allenai.pdffigures2.FigureExtractor
import org.apache.pdfbox.pdmodel.PDDocument

/** Created by schoudhury on 7/21/16.
  */
object AllenAITableCreator {

  def apply(pdLoc: String) = {
    val doc = PDDocument.load(new File(pdLoc))
    val figureExtractor = FigureExtractor()
    val document = figureExtractor.getFiguresWithText(doc)
    document.figures.foreach(x => println(s"[pageNo]:${x.page} [figureType]: ${x.figType} " +
      s"[boundary]: ${x.regionBoundary},[content]: ${x.imageText}" +
      s"[caption]: ${x.caption}"))
  }

  def main(args: Array[String]): Unit = {
    val pdLoc = "/Users/schoudhury/codes/factify/pdftabletorelationaltriple/src/test/resources/pdfs/10.1.1.10.4597.pdf"
    AllenAITableCreator(pdLoc)
  }

}
