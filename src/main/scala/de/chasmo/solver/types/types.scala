package de.chasmo.solver
package types


/**
 * Possible Movements
 */
sealed abstract class Move
case object Up    extends Move
case object Right extends Move
case object Down  extends Move
case object Left  extends Move
object Move {
  val moves = List(Left, Right, Down, Up)
}

/**
 * A position on the grid
 * @param x the column, 0-based, left-to-right
 * @param y the row, 0-based, top-to-bottom
 */
case class Pos(x: Int, y: Int) {
  def move(vector: Pos) = Pos(x + vector.x, y + vector.y)
  def +(vector: Pos) = move(vector)

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
