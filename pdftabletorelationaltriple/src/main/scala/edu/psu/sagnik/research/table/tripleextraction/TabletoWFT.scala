package edu.psu.sagnik.research.table.tripleextraction

import edu.psu.sagnik.research.table.model._

/** Created by schoudhury on 8/21/15.
  */

// scalastyle:off
object TabletoWFT {

  type A = RowHeaderCell
  def A(x: Int, y: Int, tg: TextGeneric) = RowHeaderCell(x, y, tg)

  type B = ColHeaderCell
  def B(x: Int, y: Int, tg: TextGeneric) = ColHeaderCell(x, y, tg)

  type C = DataCell
  def C(x: Int, y: Int, tg: TextGeneric, rowpath: Seq[A], colpath: Seq[B]) = DataCell(x, y, tg, rowpath, colpath)

  def cellClassify(table: Table): Option[WFT] = {
    val wft = WFT(table.pageNumber, table.bb, table.content, table.caption, table.context,
      Seq.empty[A], Seq.empty[B], Seq.empty[C])

    CriticalCell.getCriticalCell(table) match {
      case Some(criticalcell) => {
        Some(
          table.cells.foldLeft(wft) {
            case (wft @ WFT(table.pageNumber, table.bb, table.content, table.caption, table.context, rhcs, chcs, dcs), cell) => {
              //the ordering is important here.
              if (cell.startCol < criticalcell.startCol) wft.copy(rhcs = rhcs :+ A(cell.startRow, cell.startCol, cell.tg))
              else if (cell.startRow < criticalcell.startRow) wft.copy(chcs = chcs :+ B(cell.startRow, cell.startCol, cell.tg))
              else wft.copy(dcs = dcs :+ C(cell.startRow, cell.startCol, cell.tg, Nil, Nil))
            }
          }
        )
      }
      case None => None
    }
  }
  //TODO: some cells will be in the "stub" region. What to do with them?
  def headerPathstoDataCells(table: Table): Option[WFT] = {
    cellClassify(table) match {
      case Some(wft) => Some(
        wft.copy(dcs = wft.dcs.map(dc => dc.copy(
          rowpath = getRowPath(dc, wft),
          colpath = getColPath(dc, wft)
        )))
      )
      case None => None
    }
  }

  def getColPath(dc: C, wft: WFT): Seq[B] = wft.chcs.filter(x => x.startRow < dc.startRow && x.startCol <= dc.startCol
    && wft.chcs.filter(y => y.startCol <= dc.startCol && y.startRow == x.startRow && y.startCol > x.startCol).isEmpty).sortWith(_.startRow < _.startRow)

  def getRowPath(dc: C, wft: WFT): Seq[A] = wft.rhcs.filter(x => x.startCol < dc.startCol && x.startRow <= dc.startRow
    && wft.rhcs.filter(y => y.startRow <= dc.startRow && y.startCol == x.startCol && y.startRow > x.startRow).isEmpty).sortWith(_.startCol < _.startCol)

}
