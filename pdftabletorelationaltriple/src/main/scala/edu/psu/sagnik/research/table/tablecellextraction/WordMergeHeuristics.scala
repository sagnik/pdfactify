package edu.psu.sagnik.research.table.tablecellextraction

import edu.psu.sagnik.research.table.model.{ Rectangle, TextGeneric }

/** Created by schoudhury on 8/20/15.
  */

// scalastyle:off
object WordMergeHeuristics {
  type A = TextGeneric

  def mergeThresholdWholeMedian(words: Seq[A]): Float = {
    val x1s = words.map(w => w.bb.x1)
    val x1dists = x1s.combinations(2).toList.map { case List(a, b) => a - b }.filter(_ > 0).sorted
    x1dists(x1dists.length / 2)
  }

  def mergeThresholdWholeMedianParametric(words: Seq[A]): Float = {
    val THRESHOLD = 0.25
    val x1s = words.map(w => w.bb.x1)
    val x1dists = x1s.combinations(2).toList.map { case List(a, b) => a - b }.filter(_ > 0).sorted
    (THRESHOLD * x1dists(x1dists.length / 2)).toFloat
  }

  def mergeThresholdLineMedian(words: Seq[A]): Float = {
    val seqpairwords = words.combinations(2).toList
      .filter { case List(a, b) => scala.math.abs(a.bb.y1 - b.bb.y1) <= 1 && scala.math.abs(a.bb.y2 - b.bb.y2) <= 1 }
    //println(seqpairwords)
    val x1dists = seqpairwords.map {
      case List(a, b) => {
        val (left, right) = if (a.bb.x1 > b.bb.x2) (b, a) else (a, b)
        right.bb.x1 - left.bb.x2
      }
    }.filter(_ > 0).sorted
    //println(x1dists)
    x1dists(x1dists.length / 2)
  }

  def closestRightWord(x: A, words: Seq[A]): A = {
    val distsortedcells = words.filter {
      a =>
        a.bb != x.bb &&
          scala.math.abs(a.bb.y1 - x.bb.y1) <= 1 && scala.math.abs(a.bb.y2 - x.bb.y2) <= 1 &&
          a.bb.x1 > x.bb.x2
    }
      .map(a => (a, Rectangle.rectDistance(a.bb, x.bb)))
      .sortWith(_._2 < _._2)
    if (distsortedcells.isEmpty) x //there's no word to the right
    else distsortedcells(0)._1
  }

  def mergeThresholdWordMedian(words: Seq[A]): Float = {
    val seqpairwords = words.map(x => (x, closestRightWord(x, words)))
    //seqpairwords.foreach(x=>println(s"word ${x._1.content}: closest word: ${x._2.content}"))
    val x1dists = seqpairwords.map {
      case (a, b) => {
        val (left, right) = if (a.bb.x1 > b.bb.x2) (b, a) else (a, b)
        //println(s"distance between ${left.content} & ${right.content} is ${right.bb.x1-left.bb.x2}")
        right.bb.x1 - left.bb.x2
      }
    }.distinct.filter(_ > 0).sorted
    //print(x1dists)
    //print(x1dists.length)
    //print(words.length)
    //sum(x1dists)/x1dists.length
    if (x1dists.nonEmpty) (0.25 * x1dists(x1dists.length / 2)).toFloat else 0
  }

}
