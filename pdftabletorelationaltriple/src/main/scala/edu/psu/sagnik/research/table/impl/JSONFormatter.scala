package edu.psu.sagnik.research.table.impl

import edu.psu.sagnik.research.allenaiconversion.Rectangle
import edu.psu.sagnik.research.table.model.WFT
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

/** Created by schoudhury on 8/26/15.
  */
object JSONFormatter {

  def precReduce(d: Float): Double = BigDecimal.decimal(d).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble

  def precReduce(r: Rectangle): List[Double] = List(precReduce(r.x1), precReduce(r.y1), precReduce(r.x2), precReduce(r.x1))

  def wftToJsonString(wft: WFT): String = {
    val jsoncontent =
      (
        ("caption" -> wft.caption) ~
        ("mention" -> wft.context) ~
        ("pagenumber" -> wft.pageNumber) ~
        ("bb" -> precReduce(wft.bb)) ~
        ("content" -> wft.content) ~
        ("colheadercells" ->
          wft.chcs.map { c =>
            (
              ("startRow" -> c.startRow) ~
              ("startCol" -> c.startCol) ~
              ("content" -> c.tg.content) ~
              ("bb" -> precReduce(c.tg.bb))
            )
          }) ~
          ("rowheadercells" ->
            wft.rhcs.map { r =>
              (
                ("startRow" -> r.startRow) ~
                ("startCol" -> r.startCol) ~
                ("content" -> r.tg.content) ~
                ("bb" -> precReduce(r.tg.bb))
              )
            }) ~
            ("datacells" ->
              wft.dcs.map { d =>
                (
                  ("startRow" -> d.startRow) ~
                  ("startCol" -> d.startCol) ~
                  ("content" -> d.tg.content) ~
                  ("bb" -> precReduce(d.tg.bb)) ~
                  ("rowheaderpath" -> { val org = d.rowpath.foldLeft("")((a, b) => a + b.tg.content + ":"); if (org.length == 0) org else org.substring(0, org.length - 1) }) ~
                  ("colheaderpath" -> { val org = d.colpath.foldLeft("")((a, b) => a + b.tg.content + ":"); if (org.length == 0) org else org.substring(0, org.length - 1) })
                )
              })
      )
    compact(render(jsoncontent))
  }

}
