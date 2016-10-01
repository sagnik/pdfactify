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
      s"[boundary]: ${x.regionBoundary} [caption]: ${x.caption}" +
      s"[content]: ${x.imageText.map(_.text)}" +
      s"\n------------------------------\n"))

  }

  def main(args: Array[String]): Unit = {
    //val pdLoc = "/Users/schoudhury/data/econpapers/ageconsearch.umn.edu/pdfs/1.pdf"
    val pdLoc = "/home/sagnik/data/nlp-table-data/pdfs/W04-1603.pdf"
    AllenAITableCreator(pdLoc)
  }

}
