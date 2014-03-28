package de.chasmo.solver
package game

import types._

import scala.annotation.tailrec


/**
 * Base trait for some game related function
 */
trait GameLike {
  protected case class MovePos(forMerge: Pos, nonMerge: Pos)

  import Game.History

  /**
   * Generate a random tile based on the provided settings ant the grif
   * @param grid    the current grid
   * @param rates   a list of rates that determine what tiles to be places with which probability
   * @param random  if true, place tiles at random. otherwise, place new tiles at
   *                deterministic places.
   * @return        None, if no tile could be places, otherwise Some of the new tile
   */
  def randomTile(grid: Grid, rates: List[(Double, Num)], random: Boolean): Option[Tile] =
    if (random) {
      grid.randomAvailable().flatMap { pos =>
        val r = math.random
        rates.collectFirst {
          case (rate, num) if r >= rate => Tile(pos, num)
        }
      }
    } else grid.availableCells.headOption.map { p => Tile(p, `2`) }


  /** initializes a grid with n random tiles */
  def initGrid(size: Int, startTiles: Int, rates: List[(Double, Num)], random: Boolean): Grid = {
    @tailrec def init0(g: Grid, tilesToPlace: Int): Grid = tilesToPlace match {
      case x if x > 0 => init0(randomTile(g, rates, random).map(g + _).getOrElse(g), x - 1)
      case _ => g
    }

    init0(Grid(size), startTiles)
  }

  /**
   * perform a move, if possible
   * @param grid    the [[Grid]] that should be moved
   * @param move    the [[Move]] that should be performed
   * @param score   the current score
   * @return        if the move was possible, return Some [[History]], otherwise None
   */
  def moveAround(move: Move, score: Int)(implicit grid: Grid): Option[History] = {
    val vector = getVector(move)
    val allTraversals = getTraversals(move)

    /**
     * Recursively update the grid
     * @param g           the per-recursion-current grid
     * @param traversals  a list of positions that may have tiles that still need to be traversed
     * @param mergedPos   a set of positions that were merged during this move's processing
     * @param score       the cumulative score of this move processing
     * @param moved       true, of there happened at least on movement
     * @return            a Tuple3 of the new Grid, the final score and whether or not there happened at least on movement
     */
    @tailrec def move0(g: Grid, traversals: List[Pos], mergedPos: Set[Pos], score: Int, moved: Boolean): (Grid, Int, Boolean) = traversals match {
      case cell :: tail =>
        g(cell) match {
          case None       => move0(g, tail, mergedPos, score, moved)
          case Some(tile) =>

            val moveTarget = findFarthestPos(cell, vector, validVacant(g))
            val maybeMerge = g(moveTarget.forMerge).filter(_.num.value == tile.num.value).filterNot(n => mergedPos(n.pos))

            maybeMerge match {
              case None         =>
                if (tile.pos == moveTarget.nonMerge) move0(g, tail, mergedPos, score, moved)
                else move0(g.move(tile, tile moveTo moveTarget.nonMerge), tail, mergedPos, score, moved = true)
              case Some(target) =>

                val merged = tile merge target.pos

                move0(g.move(tile, merged), tail, mergedPos + target.pos, score + merged.value, moved = true)
            }
        }
      case Nil  => (g, score, moved)
    }

    val (newGrid, addScore, hasMoved) = move0(grid, allTraversals, Set.empty, 0, moved = false)

    if (hasMoved) Some(History(newGrid, Some(move), score + addScore))
    else None
  }

  /**
   * @param grid the current grid
   * @return  true, of there is at least one winning tile
   */
  def isWin(implicit grid: Grid): Boolean = grid.exists(_.exists(_.num.wins))

  /**
   * A 1d-vector representing the direction of the move
   * Tiles will be moved step by step by this vector
   *
   * @param move in which direction to move
   * @return an 1d-vector for the direction
   */
  protected def getVector(move: Move) = move match {
    case Up    => Pos(0, -1)
    case Right => Pos(1, 0)
    case Down  => Pos(0, 1)
    case Left  => Pos(-1, 0)
  }

  /**
   * The list of traversals for a given direction. The order is important here.
   * That is, if you move to the right, the rightmost tiles are checked before the leftmost ones.
   * Given three tiles that could be merged:
   *    + [    ] | [   2] | [   2] | [   2] +
   * Moving to the right would merge the two rightmost tiles
   *    + [    ] | [    ] | [   2] | [   4] +
   * while moving to the left would merge the two leftmost tiles
   *    + [   4] | [   2] | [    ] | [    ] +
   * The same goes for Up and Down
   *
   * @param move in which direction to move
   * @return A list of positions in which order these should be processed
   */
  protected def getTraversals(move: Move)(implicit grid: Grid): List[Pos] = {
    val ts = move match {
      case Up    => leftToRight -> topToBottom
      case Right => rightToLeft -> topToBottom
      case Down  => leftToRight -> bottomToTop
      case Left  => leftToRight -> topToBottom
    }

    ts._1.flatMap(x => ts._2.map(y => Pos(x, y))).toList
  }

  /**
   * Find the final position for a given
   * @param pos    the start position
   * @param vector the movement vector, see [[getVector()]]
   * @param valid  a predicate that is used to determine when a postion isn't valid anymore
   * @return       a Tuple2 of two position.
   *               The first one is either out of bound or points to an occupied position,
   *                which would result in a merge
   *               The second one points to the farthest possible valid position.
   *                It is guaranteed that this position is both, on the grid and vacant
   *                (or might just be the start position)
   *
   */
  protected def findFarthestPos(pos: Pos, vector: Pos, valid: Pos => Boolean): MovePos = {
    @tailrec def find0(cell: Pos, prev: Pos): MovePos =
      if (!valid(cell)) MovePos(cell, prev)
      else find0(cell + vector, cell)

    find0(pos + vector, pos)
  }

  /**
   * @param grid  the [[Grid]] to validate
   * @return  a validation function, that validated empty grid cells
   */
  protected def validVacant(implicit grid: Grid): Pos => Boolean = p => grid.valid(p) && grid.isEmpty(p)

  /**
   * @param grid  the [[Grid]] to validate
   * @return  a validation function, that validated non-empty grid cells
   */
  protected def validOccupied(implicit grid: Grid): Pos => Boolean = p => grid.valid(p) && grid.nonEmpty(p)

  /** handy index ranges representing a traversal order */
  protected def leftToRight(implicit grid: Grid) = 0 until grid.size
  protected def rightToLeft(implicit grid: Grid) = grid.size - 1 to 0 by -1
  protected def topToBottom(implicit grid: Grid) = 0 until grid.size
  protected def bottomToTop(implicit grid: Grid) = grid.size - 1 to 0 by -1
}

object GameLike extends GameLike
