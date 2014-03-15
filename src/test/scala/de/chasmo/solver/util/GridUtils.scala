package de.chasmo.solver.util

import de.chasmo.solver.types._
import de.chasmo.solver.types.Pos
import de.chasmo.solver.types.Tile

object GridUtils {

  val numMappings = Map(
    2 -> `2`,
    4 -> `4`,
    8 -> `8`,
    16 -> `16`,
    32 -> `32`,
    64 -> `64`,
    128 -> `128`,
    256 -> `256`,
    512 -> `512`,
    1024 -> `1024`,
    2048 -> `2048`
  )

  def create(rows: List[Int]*): Grid = {
    val colSizes = rows.map(_.size)
    val rowSize = rows.size

    require(colSizes.distinct.size == 1, "All columns must be of equals size")
    require(colSizes.head == rowSize, "rows and columns must bo of equal size")

    var g = Grid(rowSize)

    for {
      (row, y) <- rows.zipWithIndex
      (num, x) <- row.zipWithIndex
    } {
      g = g.updated(Pos(x, y), numMappings.get(num).map(n => Tile(Pos(x, y), n)))
    }

    g
  }

  def cells(grid: Grid): List[List[Int]] = {
    val rows = Array.fill(grid.size)(Array.fill(grid.size)(0))
    for {
      y <- 0 until grid.size
      x <- 0 until grid.size
    } {
      grid(Pos(x, y)) match {
        case Some(tile) => rows(y)(x) = tile.num.value
        case None =>
      }
    }

    rows.map(_.toList).toList
  }
}
