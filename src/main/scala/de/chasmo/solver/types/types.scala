package de.chasmo.solver
package types

import scala.util.Random


/**
 * Possible Movements
 */
sealed abstract class Move
case object Up    extends Move
case object Right extends Move
case object Down  extends Move
case object Left  extends Move
object Move {
  val moves = List(Up, Right, Down, Left)
}

/**
 * A Number that os displayed
 */
sealed trait Num {
  val next: Num
  val value: Int
  val wins: Boolean = false

  override def toString = "[%4s]" format value
}
/** Why numbers? Because we can! */
case object `2048` extends Num {
  val value = 2048
  val next = this

  override val wins = true
}
case object `1024` extends Num {
  val value = 1024
  val next = `2048`
}
case object `512` extends Num {
  val value = 512
  val next = `1024`
}
case object `256` extends Num {
  val value = 256
  val next = `512`
}
case object `128` extends Num {
  val value = 128
  val next = `256`
}
case object `64` extends Num {
  val value = 64
  val next = `128`
}
case object `32` extends Num {
  val value = 32
  val next = `64`
}
case object `16` extends Num {
  val value = 16
  val next = `32`
}
case object `8` extends Num {
  val value = 8
  val next = `16`
}
case object `4` extends Num {
  val value = 4
  val next = `8`
}
case object `2` extends Num {
  val value = 2
  val next = `4`
}

/**
 * A position on the grid
 * @param x the column, 0-based, left-to-right
 * @param y the row, 0-based, top-to-bottom
 */
case class Pos(x: Int, y: Int) {
  def move(vector: Pos) = Pos(x + vector.x, y + vector.y)

  override def toString = s"($x;$y)"
}

/**
 * A concrete tile on the grid, combining the position and the value
 * @param pos  The position on the grid
 * @param num  The value/number on this tile
 */
case class Tile(pos: Pos, num: Num) {
  def value = num.value

  def moveTo(p: Pos) = copy(pos = p)
  def merge(p: Pos) = Tile(pos = p, num = num.next)

  def posEq(other: Tile) = pos == other.pos
  def valEq(other: Tile) = num.value == other.num.value
}

/**
 * A square grid that serves as the play board
 *
 * A grid wraps a 2d-vector of Option[Tile] where an empty tile is represented by None
 */
class Grid private (val size: Int, cells: Vector[Vector[Option[Tile]]]) {
  /**
   * The primary (public) constructor
   * @param size the length of one side
   * @return a new grid with empty tiles
   */
  def this(size: Int) = this(size, Vector.fill(size, size)(None))

  private val r = new Random()

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
    else cells(pos.x)(pos.y)
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
    new Grid(size, cells.updated(pos.x, cells(pos.x).updated(pos.y, tile)))
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
  def exists(f: Option[Tile] => Boolean): Boolean = cells.exists(_.exists(f))


  private def collect[U](f: PartialFunction[(Pos, Option[Tile]), U]): Vector[U] = withPos.collect(f)

  private def withPos: Vector[(Pos, Option[Tile])] = cells.zipWithIndex.flatMap {
    case (col, x) => col.zipWithIndex.map {
      case (row, y) => Pos(x, y) -> row
    }
  }
}

object Grid {
  def apply(size: Int) = new Grid(size)
}
