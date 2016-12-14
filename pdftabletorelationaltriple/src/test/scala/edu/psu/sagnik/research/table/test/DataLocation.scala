package edu.psu.sagnik.research.table.test

import java.io.File

/** Created by schoudhury on 8/21/15.
  */
object DataLocation {
  //val jsonLoc = "src/test/resources/jsons/10.1.1.10.1035-Table-2.json"
  //val jsonLoc = "src/test/resources/jsons/10.1.1.194.433-Table-2.json"
  //val jsonLoc = "src/test/resources/jsons/10.1.1.106.5870-Table-4.json"
  //val jsonLoc = "src/test/resources/jsons/10.1.1.159.3090-Table-7.json"
  //val jsonLoc = "src/test/resources/jsons/N10-1104-Table-1.json"
  val baseFile = "W11-2916" //"N10-1104"
  val tableBase = "-Table-"
  val tableID = tableBase + "5"

/*
  val basePDir = "/Users/schoudhury/data/econpapers/ageconsearch.umn.edu/pdfs/" //"src/test/resources/pdfs/"
  val baseSVGDir = "/Users/schoudhury/data/econpapers/ageconsearch.umn.edu/tablesvgs/" //"src/test/resources/svgs/"

  val baseJsonDir = "/home/sagnik/data/nlp-table-data/randjsons/" //"src/test/resources/jsons/"
  val baseImageDir = "/home/sagnik/data/nlp-table-data/randpngs/" //"src/test/resources/images/"
*/

  val basePDir = "/media/sagnik/OS_Install/data/nlp-table-data/pdfs/" //"src/test/resources/pdfs/"
  val baseSVGDir = "/Users/schoudhury/data/econpapers/ageconsearch.umn.edu/tablesvgs/" //"src/test/resources/svgs/"

  val baseJsonDir = "/media/sagnik/OS_Install/data/nlp-table-data/jsons/" //"src/test/resources/jsons/"
  val baseImageDir = "/home/sagnik/data/nlp-table-data/randpngs/" //"src/test/resources/images/"

  val jsonLoc = baseJsonDir + baseFile + tableID + ".json"
  val pdfLoc = basePDir + baseFile + ".pdf"

  val imageLoc = baseImageDir + baseFile + tableID + ".png"

  val svgLoc = baseSVGDir + baseFile + tableID + ".svg"

  import scala.util.matching.Regex
  def recursiveListFiles(f: File, r: Regex): Array[File] = {
    val these = f.listFiles
    val good = these.filter(f => r.findFirstIn(f.getName).isDefined)
    good ++ these.filter(_.isDirectory).flatMap(recursiveListFiles(_, r))
  }
}
