package edu.psu.sagnik.research.table.model

/** Created by schoudhury on 8/13/15.
  */
case class Rectangle(x1: Float, y1: Float, x2: Float, y2: Float)

/** Evaluates to a string that contains the Rectangle's
  * point information as x1,y2,x2,y2 .
  */
object Rectangle {
  def asCoordinatesStr(r: Rectangle): String =
    s"${r.x1},${r.y1},${r.x2},${r.y2}"

  def rectInterSects(r1: Rectangle, r2: Rectangle): Boolean = {
    r1 != r2 && r1.x1 <= r2.x2 && r2.x1 <= r1.x2 && r1.y1 <= r2.y2 && r2.y1 <= r1.y2
  }

  //TODO: check correctness
  def rectInside(in: Rectangle, out: Rectangle): Boolean = in.x1 >= out.x1 && in.y1 >= out.y1 && in.x2 <= out.x2 && in.y2 <= out.y2

  //a bit of caution: this works only for axes parallel rectangles.
  // That suffice for our purpose, but this isn't a generic method.
  def rectDistance(r1: Rectangle, r2: Rectangle): Float = {
    val dy1 = if (r1.y2 < r2.y1) r2.y1 - r1.y2 else 0
    val dy2 = if (r1.y1 > r2.y2) r1.y1 - r2.y2 else 0
    val dx1 = if (r1.x2 < r2.x1) r2.x1 - r1.x2 else 0
    val dx2 = if (r1.x1 > r2.x2) r1.x1 - r2.x2 else 0
    dx1 + dx2 + dy1 + dy2
  }

}
