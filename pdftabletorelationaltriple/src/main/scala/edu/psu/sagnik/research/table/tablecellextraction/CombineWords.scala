package edu.psu.sagnik.research.table.tablecellextraction

/** Created by schoudhury on 8/13/15.
  */
/*
we will take a JSON file that contains table boundary and the bounding boxes
of the words inside it. Words will be horizontally combined to form what a cell
content. Multiiline cells are not considered at this point. (It would be awesome to
combine them vertically as well, but that seems hard at this moment).

We are currently using the output from pdffigures code from allenai (https://github.com/allenai/pdffigures)
This does a really good job in producing table boundaries in scholarly papers. The case classes
in TableADT.scala show the JSON schema.
If you change that to Roman's output, you have to first figure out a way to write
or produce a JSON file with the case class schema in model.IntermediateTable. For an example,
see model.AllenAIDataConversion
*/

// scalastyle:off
import java.util.logging.Logger

import edu.psu.sagnik.research.allenaiconversion.{IntermediateTable, Rectangle, TextGeneric}
import edu.psu.sagnik.research.pdsimplify.path.model.{PDLine, PDSegment}
import edu.psu.sagnik.research.table.model.{IntermediateTable, TextGeneric}
import org.allenai.common.Logging

object CombineWords extends Logging {

  type A = TextGeneric
  def A(x: String, y: Rectangle) = TextGeneric(x, y)

  def wordMergedTable(table: IntermediateTable): IntermediateTable = table.copy(
    textSegments = horizontalMerge(table.textSegments, table.pdLines, WordMergeHeuristics.mergeThresholdWordMedian)
  )

  /* this function will be changed with linear chain CRFs to facilitate merging*/
  def horizontalMerge(words: Seq[A], pdLines: Seq[PDSegment], f: Seq[A] => Float): Seq[A] = {
    val threshold = f(words) + 4f //added because we are reducing the original boundaries by 2.
    logger.debug(s"The horizontal distance threshold for merging words is $threshold")
    merge(words, Nil, threshold, pdLines)
  }

  def mergedWord(x: A, y: A): A = {
    val (left, right) = if (y.bb.x2 < x.bb.x1) (y, x) else (x, y)
    A(
      left.content + " " + right.content,
      Rectangle(left.bb.x1, List(x.bb.y1, y.bb.y1).min, right.bb.x2, List(x.bb.y2, y.bb.y2).max)
    )
  }

  implicit def min(x: Float, y: Float) = if (x > y) y else x
  implicit def max(x: Float, y: Float) = if (x > y) x else y

  def lineIntersects(x: A, y: A, pdLines: Seq[PDSegment]) = {
    if (pdLines.isEmpty) false
    else pdLines.exists(line => Rectangle.rectInterSects(
      Rectangle(
        line.bb.x1,
        line.bb.y1,
        line.bb.x2,
        line.bb.y2
      ),
      Rectangle(
        min(x.bb.x1, y.bb.x1),
        min(x.bb.y1, y.bb.y1),
        max(x.bb.x2, y.bb.x2),
        max(x.bb.y2, y.bb.y2)
      )
    ))

  }
  def shouldBeMerged(x: A, y: A, threshold: Float, pdLines: Seq[PDSegment]): Boolean = {
    val (left, right) = if (y.bb.x2 < x.bb.x1) (y, x) else (x, y)
    //println(s"${left.content} and ${right.content} separated by a line? ${lineIntersects(left,right,pdLines)}")
    isSubSuperscript(left, right) || (
      right.bb.x1 - left.bb.x2 < threshold &&
      scala.math.abs(y.bb.y1 - x.bb.y1) <= 2
    ) && //to ensure that the merged words are from the same vertical line
      !lineIntersects(left, right, pdLines)
  }

  def isSubSuperscript(left: A, right: A): Boolean = (right.bb.y2 - right.bb.y1) < 0.75 * (left.bb.y2 - left.bb.y1) &&
    right.bb.x1 - left.bb.x2 < 6 &&
    right.bb.x1 - left.bb.x2 > 0 && //very close horizontally but to the right
    (right.bb.y1 > left.bb.y2 || right.bb.y2 > left.bb.y1) //subscript or superscrpt

  def getMergingElement(x: A, words: Seq[A], threshold: Float, pdLines: Seq[PDSegment]): Option[(A, A)] = {
    val sortedwords = words.sortWith(_.bb.x1 < _.bb.x1)
    val word = sortedwords.find(y => x != y && shouldBeMerged(x, y, threshold, pdLines))
    word match {
      case Some(matchedword) => Some((mergedWord(x, matchedword), matchedword))
      case None => None
    }
  }

  /* All the elements in the list are unique. Will check if there is a merging possibility with the
   elements, and choose the one that is horizontally closest.
    */

  def merge(l: Seq[A], accum: Seq[A], threshold: Float, pdLines: Seq[PDSegment]): Seq[A] = {
    l match {

      case x :: ys =>
        val toBeMerged = getMergingElement(x, l, threshold, pdLines)
        toBeMerged match {
          case Some((merged, toBeRemoved)) =>
            merge(ys.filterNot(word => word == toBeRemoved) :+ merged, accum, threshold, pdLines)
          case None =>
            merge(ys, accum :+ x, threshold, pdLines)

        }

      case Nil => accum
    }
  }

}
