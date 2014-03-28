package de.chasmo.solver
package types

import scala.util.Random

/**
 * A square grid that serves as the play board
 *
 * A grid wraps a linearized 2d-vector of Option[Tile] where an empty tile is represented by None
 */
class Grid private (val size: Int, cells: Vector[Option[Tile]]) {
  /**
   * The primary (public) constructor
   * @param size the length of one side
   * @return a new grid with empty tiles
   */
  def this(size: Int) = this(size, Vector.fill(size * size)(None))

  final private val r = new Random()

  final private implicit def pos2index(pos: Pos) = (pos.y * size) + pos.x
  final private def index2pos(idx: Int) = Pos(idx % size, idx / size)

  /**
   * Valid positions must exists on this grid
   * @param pos any position
   * @return true if the position is on the grid, otherwise false
   */
  def valid(pos: Pos): Boolean =
    pos.x >= 0 && pos.x < size && pos.y >= 0 && pos.y < size

  /**
   * Get a tile that may exist on the given position
   * @param pos the position of the tile
   * @return None if there is no tile on this position or the position is invalid, otherwise Some(tile)
   */
  def apply(pos: Pos): Option[Tile] = {
    if (!valid(pos)) None
    else cells(pos)
  }

  /**
   * @param pos the position of the tile
   * @return false if there is no tile on this position or the position is invalid, otherwise true
   */
  def isEmpty(pos: Pos): Boolean = apply(pos).isEmpty
  def nonEmpty(pos: Pos): Boolean = !isEmpty(pos)

  /**
   * @return true if there is any free tile available, false if the grid is full
   */
  def isEmpty: Boolean = exists(_.isEmpty)
  def nonEmpty = !isEmpty

  /**
   * Update the grid on a position
   * @param pos the postion to update
   * @param tile the new tile. If this is None, this will remote the tile from the grid.
   *             If this is Some(tile), it will set the tile on the grid.
   *             Previous tenant in the position are evicted without mercy!
   * @return the new Grid with the updated tile
   */
  def updated(pos: Pos, tile: Option[Tile]): Grid = {
    require(valid(pos))
    new Grid(size, cells.updated(pos, tile))
  }

  /** shortcut for adding a tile on its own position */
  def add(tile: Tile): Grid = updated(tile.pos, Some(tile))
  def +(tile: Tile): Grid = add(tile)

  /** shortcut for removing whatever is on this position */
  def remove(pos: Pos): Grid = updated(pos, None)
  def -(pos: Pos): Grid = remove(pos)

  /** shortcut for mpving one tile around.
    * This is implemented as deleting the old til, then adding the new tile */
  def move(from: Tile, to: Tile) = remove(from.pos).add(to)

  /**
   * @return a position of some randomly chosen vacant spot on the grid
   */
  def randomAvailable(): Option[Pos] = r.shuffle(availableCells).headOption

  /**
   * @return all positions of vacant spots on the grid
   */
  def availableCells: Vector[Pos] = collect { case (pos, None) => pos }

  /**
   * Tests whether a predicate holds for some of the tiles on this grid
   * @param f the predicate used to test teh tiles
   * @return  true if there is at least one element for which f holds, otherwise false
   */
  def exists(f: Option[Tile] => Boolean): Boolean = cells.exists(f)

  /**
   * Execute a partialFunction for each (pos -> content) tuple
   * @param f     the partial function, if it is defined, the value is collected
   * @tparam U    the return type of the result collection
   * @return      a Vector with some Us collected
   */
  def collect[U](f: PartialFunction[(Pos, Option[Tile]), U]): Vector[U] = withPos.collect(f)


  private def withPos: Vector[(Pos, Option[Tile])] = cells.zipWithIndex.map {
    case (tile, idx) => index2pos(idx) -> tile
  }
}

object Grid {
  def apply(size: Int) = new Grid(size)
}
