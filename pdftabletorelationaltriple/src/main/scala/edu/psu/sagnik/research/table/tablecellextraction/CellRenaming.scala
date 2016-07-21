package edu.psu.sagnik.research.table.tablecellextraction

/** Created by schoudhury on 8/18/15.
  */

// scalastyle:off
import edu.psu.sagnik.research.table.model._
/*We assume that the intermediate table has text blocks (TextGeneric) that are equivalent to cells. We are going to take these
and put a row number and column number making them "cells"*/
object CellRenaming {
  def produceRowColNumbers(itable: IntermediateTable): Table = {

    val debug = false
    //println(itable.textSegments.length)
    val candidatecells = itable.textSegments.filterNot(x =>
      hitsRightExists(x, itable.textSegments) ||
        hitsDownExists(x, itable.textSegments))

    if (debug) { println(s"candidates ${candidatecells}") }

    val lowerbottomcell = {
      if (candidatecells.isEmpty) itable.textSegments(0)
      if (candidatecells.length == 1)
        candidatecells(0)
      else candidatecells.sortWith(_.bb.x2 > _.bb.x2)(0)
    }

    if (debug) { println(s"lower bottom cell: ${lowerbottomcell.content}") }

    val tablecells = roWColRecursive(List(UnClassifiedCell(0, 0, lowerbottomcell)), List(UnClassifiedCell(0, 0, lowerbottomcell)), itable.textSegments)

    val maxrow = tablecells.sortWith(_.startRow > _.startRow)(0).startRow
    val maxcol = tablecells.sortWith(_.startCol > _.startCol)(0).startCol
    val modtablecells = tablecells.map(x => x.copy(startRow = maxrow - x.startRow, startCol = maxcol - x.startCol))

    Table(itable.pageNo, itable.bb, itable.textSegments.map(w => w.content + " ").mkString,
      modtablecells, itable.caption, itable.mention)

  }

  type A = TextGeneric
  def A(x: String, y: Rectangle) = TextGeneric(x, y)
  type B = UnClassifiedCell
  def B(x: Int, y: Int, s: A) = UnClassifiedCell(x, y, s)

  /*
This essentially is a single source longest path problem (SSLP), repeated for all cells.
Each cell is a node, the source is the lower bottom cell. An edge is created each time
you hit a cell up or left. SSLP is exponential in general, but plynomial for DAG and we are
creating the DAG on the fly. Some complexity is added because we grow a graph of "UnClassifiedCell"s
from a list of "TextGenerics"

Algo
------------
We have a list (l) of interim cells that is initiliazed with the lower bottom cell.
At each iteration, pick up a cell c from l,
see if it hits left (cl) or hits up (cu) with another cell.
If cl==None,
  add c to accum if it wasn't there.
else
  if cl wasn't in the accumulator,
    add cl in the accumulator
    with cl.row=c.row and cl.col=c.col+1.
else
  check if cl.row<c.row or cl.col<c.col+1.
  if false, do nothing.
  else
    remove cl from accumlator and add the changed cl(c.row,c.col+1) to accumulator.
    also, add cl to list l because now it can potentially change all existing cells in accumulator.

repeat the process for cu as well.
*/

  def roWColRecursive(l: List[B], accum: List[B], allwords: Seq[A]): List[B] = {
    val debug = false
    l match {
      case c :: cs => {
        val c1 = returnHitCell(hitsLeft(c, allwords), accum, c, hitsLeft = true)
        val c2 = returnHitCell(hitsUp(c, allwords), accum, c, hitsLeft = false)
        if (debug) println(s"trying cell ${c.startRow},${c.startCol},${c.tg.content}")
        (c1, c2) match {
          case (Some(cl), Some(cu)) => {
            if (debug) {
              println(s"adding both ${cl.startRow},${cl.startCol},${cl.tg.content} &" +
                s"${cu.startRow},${cu.startCol},${cu.tg.content}")
            }
            roWColRecursive(
              (cs.filterNot(x => x.tg.bb == cl.tg.bb) :+ cl).filterNot(x => x.tg.bb == cu.tg.bb) :+ cu,
              (accum.filterNot(x => x.tg.bb == cl.tg.bb) :+ cl).filterNot(x => x.tg.bb == cu.tg.bb) :+ cu,
              allwords
            )
          }
          case (None, Some(cu)) => {
            if (debug) { println(s"adding up hit ${cu.startRow},${cu.startCol},${cu.tg.content}") }
            roWColRecursive(
              cs.filterNot(x => x.tg.bb == cu.tg.bb) :+ cu,
              accum.filterNot(x => x.tg.bb == cu.tg.bb) :+ cu,
              allwords
            )
          }
          case (Some(cl), None) => {
            if (debug) { println(s"adding left hit ${cl.startRow},${cl.startCol},${cl.tg.content}") }
            roWColRecursive(
              cs.filterNot(x => x.tg.bb == cl.tg.bb) :+ cl,
              accum.filterNot(x => x.tg.bb == cl.tg.bb) :+ cl,
              allwords
            )
          }
          case (None, None) => {
            if (debug) { println(s"adding none") }
            roWColRecursive(
              cs,
              accum,
              allwords
            )
          }
        }
      }
      case Nil => accum
    }
  }

  def returnHitCell(c1: Option[A], accum: List[B], c: B, hitsLeft: Boolean): Option[B] = c1 match {
    case Some(tg) => { //we did hit left or up
      val matchedcell = accum.find(cx => cx.tg == tg)
      matchedcell match {
        case None => {
          if (hitsLeft) {
            //hitsleft
            val cell = B(c.startRow, c.startCol + 1, tg)
            Some(cell)
          } else {
            val cell = B(c.startRow + 1, c.startCol, tg)
            Some(cell)
          }
        } //both lists grow
        case Some(matchedcell) => {
          if (hitsLeft) { //hits left
            if (matchedcell.startRow < c.startRow || matchedcell.startCol < c.startCol + 1) {
              val cell = matchedcell.copy(startRow = c.startRow, startCol = c.startCol + 1)
              Some(cell)
            } else None //cs shrinks
          } else { //hits up
            if (matchedcell.startCol < c.startCol || matchedcell.startRow < c.startRow + 1) {
              val cell = matchedcell.copy(startRow = c.startRow + 1, startCol = c.startCol)
              Some(cell)
            } else None //cs shrinks
          }
        }
      }
    }
    case None => None //we didn't hit left or up
  }

  def hitsLeft(c: B, allwords: Seq[A]): Option[A] = {
    val extrect = c.tg.bb.copy(x1 = c.tg.bb.x1 - 500)
    //TODO: this is a hard coded value now. We expect rectangles to intersect within 500 points
    val interesctingRects = allwords.filter(x => x.bb != c.tg.bb && Rectangle.rectInterSects(x.bb, extrect))
      .filter(x => x.bb.x2 < c.tg.bb.x1)

    if (interesctingRects.isEmpty) {
      None
    } else {
      Some(interesctingRects.sortWith(_.bb.x2 > _.bb.x2)(0))
    }
  }

  def hitsUp(c: B, allwords: Seq[A]): Option[A] = {
    val extrect = c.tg.bb.copy(y1 = c.tg.bb.y1 - 500) //TODO: this is a hard coded value now. We expect rectangles to intersect
    //within 500 points
    val interesctingRects = allwords.filter(x => x.bb != c.tg.bb && Rectangle.rectInterSects(x.bb, extrect))
      .filter(x => x.bb.y2 < c.tg.bb.y1)
    if (interesctingRects.isEmpty) {
      None
    } else {
      Some(interesctingRects.sortWith(_.bb.y2 > _.bb.y2)(0))
    }
  }

  def hitsRightExists(c: TextGeneric, l: Seq[TextGeneric]): Boolean = {
    val extrect = c.bb.copy(x2 = c.bb.x2 + 500)
    l.filter(x => x.bb != c.bb && Rectangle.rectInterSects(x.bb, extrect))
      .filter(x => c.bb.x2 < x.bb.x1).nonEmpty

  }
  def hitsDownExists(c: TextGeneric, l: Seq[TextGeneric]): Boolean = {
    val extrect = c.bb.copy(y2 = c.bb.y2 + 500)
    /*
    if (Rectangle(271.753f,59.76001f,293.547f,75.95001f).equals(c.bb)) {
      println(l.length)
      println(extrect);
      println(l.exists(a=>a.bb.equals(Rectangle(271.753f,74.150024f,293.547f,90.339966f))))
      println(l.filter(x => x.bb != c.bb && Rectangle.rectInterSects(x.bb, extrect))
        .filter(x => c.bb.y2 < x.bb.y1))
    }
    */
    l.filter(x => x.bb != c.bb && Rectangle.rectInterSects(x.bb, extrect))
      .filter(x => c.bb.y2 < x.bb.y1).nonEmpty

  }
}
