package edu.psu.sagnik.research.table.tripleextraction

import edu.psu.sagnik.research.table.model.{ UnClassifiedCell, Table }

/** Created by schoudhury on 8/21/15.
  */

// scalastyle:off
object CriticalCell {
  def numberofCellls(startRow: Int, startCol: Int, table: Table, indicator: String): Int = {
    if (indicator == "row")
      table.cells.foldLeft(0)(
        (sameRoworCol, cell) => if (cell.startRow == startRow && cell.startCol >= startCol)
          sameRoworCol + 1 else sameRoworCol
      )
    else
      table.cells.foldLeft(0)(
        (sameRoworCol, cell) => if (cell.startCol == startCol && cell.startRow >= startRow)
          sameRoworCol + 1 else sameRoworCol
      )
  }

  def getCriticalCell(table: Table): Option[UnClassifiedCell] = {
    val maxRow = 3
    val maxCol = 3
    val startRow = 1
    val startCol = 1
    val rowIndexP = {
      (1 to maxRow).find(x => numberofCellls(x, startCol, table, "row") == numberofCellls(x - 1, startCol, table, "row")) match {
        case Some(rowIndexP) => rowIndexP
        case None => maxRow
      }
    }
    val colIndexP = {
      (1 to maxCol).find(x => numberofCellls(rowIndexP, x, table, "col") == numberofCellls(rowIndexP, x - 1, table, "col")) match {
        case Some(colIndexP) => colIndexP
        case None => maxCol
      }
    }
    table.cells.find(x => x.startRow == rowIndexP && x.startCol == colIndexP)
  }

}
