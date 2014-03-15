package de.chasmo.solver.game

import de.chasmo.solver.types._
import scala.util.Random
import scala.annotation.tailrec
import com.typesafe.config.{ConfigFactory, Config}

/**
 * The effing addicting 2048 game
 *
 * Each instance of this class represents a given state.
 * The Game is immutable and persistent, one could therefore easily jump
 * back and forth between different states
 */
class Game private[game](history: List[Game.History]) {

  /**
   * The primary (public) constructor
   * @param settings  the settings of this Game, see [[Settings]]
   * @return The Game, motherfucker, do you play it?   http://goo.gl/gmgo0o
   */
  def this(settings: Settings) = this(List(Game.History(Game.initGrid(settings.size, settings.startTiles), None, 0)))

  import Game.{randomTile, History, GameData}

  val data = GameData(score,
    gameOver = !hasMoves,
    won = grid.exists(_.exists(_.num.wins))
  )

  /**
   * @return the current grid
   */
  def grid = history.head.grid

  /**
   * @return the current score
   */
  def score = history.head.score

  /**
   * @return the last move played
   */
  def lastMove = history.head.move

  /**
   * In reaction to an input, move tiles around, possibly merging them on the way
   * @param move  in which direction to move
   * @return a new game with an updated grid and history
   */
  def move(move: Move): Game = moveAround(move, addRandomTile = true)

  private[game] def moveAround(move: Move, addRandomTile: Boolean): Game = data.terminated match {
    case true  => this
    case false =>
      val vector = getVector(move)
      val allTraversals = getTraversals(move)

      /**
       * Recursively update the grid
       * @param g           the per-recursrion-current grid
       * @param traversals  a list of positions that may have tiles that still need to be traversed
       * @param mergedPos   a set of positions that were merged during this move processings
       * @param score       the cumulative score of this move processing
       * @param moved       true, of there happened at least on movement
       * @return            a Tuple3 of the new Grid, the final score and whether or not there happened at least on movement
       */
      @tailrec def move0(g: Grid, traversals: List[Pos], mergedPos: Set[Pos], score: Int, moved: Boolean): (Grid, Int, Boolean) = traversals match {
        case cell :: tail =>
          g(cell) match {
            case None       => move0(g, tail, mergedPos, score, moved)
            case Some(tile) =>

              val (next, farthest) = findFarthestPos(cell, vector, p => g.valid(p) && g.isEmpty(p))
              val maybeMerge = g(next).filter(_.num.value == tile.num.value).filterNot(n => mergedPos(n.pos))

              maybeMerge match {
                case None         =>
                  if (tile.pos == farthest) move0(g, tail, mergedPos, score, moved)
                  else move0(g.move(tile, tile moveTo farthest), tail, mergedPos, score, moved = true)
                case Some(target) =>

                  val merged = tile merge target.pos

                  move0(g.move(tile, merged), tail, mergedPos + target.pos, score + merged.value, moved = true)
              }
          }
        case Nil if moved && addRandomTile => (randomTile(g), score, moved)
        case Nil                           => (g, score, moved)
      }

      val (newGrid, addScore, hasMoved) = move0(grid, allTraversals, Set.empty, 0, moved = false)

      if (hasMoved) {
        new Game(History(newGrid, Some(move), score + addScore) :: history)
      } else this
  }

  /**
   * go back one state
   * @return a new game, representing the previous state
   */
  def undo = history match {
    case current :: previous :: rest =>
      new Game(previous :: rest)
    case _ => this
  }

  /**
   * @return true if here are vacant place or direct merges, so one can still move
   */
  private def hasMoves = grid.isEmpty || hasDirectMerges

  /**
   * @return true if there are some adjacent tiles of the same value.
   *         Since this is only taken into account, when the game board is full,
   *         we don't need to look for tiles anywhere, just the direct vicinity
   */
  private def hasDirectMerges: Boolean =
    grid.exists(_.exists {
      tile =>
        Move.moves.map(getVector).exists {
          vector =>
            val cell = tile.pos.move(vector)
            grid(cell).exists(_ valEq tile)
        }
    })

  /**
   * A 1d-vector representing the direction of the move
   * Tiles will be moved step by step by this vector
   *
   * @param move in which direction to move
   * @return an 1d-vector for the direction
   */
  private def getVector(move: Move) = move match {
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
   * @return A list of position in which order the should be processed
   */
  private def getTraversals(move: Move): List[Pos] = {
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
   *                It is guaranteed that this position is both, on the grid or vacant
   *                (or might just be the start position)
   *
   */
  private def findFarthestPos(pos: Pos, vector: Pos, valid: Pos => Boolean): (Pos, Pos) = {
    @tailrec def find0(cell: Pos, prev: Pos): (Pos, Pos) =
      if (!valid(cell)) (cell, prev)
      else find0(cell move vector, cell)

    find0(pos move vector, pos)
  }

  /** handy index ranges representing a traversal order */
  private val leftToRight = 0 until grid.size
  private val rightToLeft = grid.size - 1 to 0 by -1
  private val topToBottom = 0 until grid.size
  private val bottomToTop = grid.size - 1 to 0 by -1

}

object Game {
  private val rand = new Random()

  /** initializes the grid with n random tiles */
  private def initGrid(size: Int, startTiles: Int): Grid = {
    @tailrec def init0(g: Grid, tilesToPlace: Int): Grid = tilesToPlace match {
      case x if x > 0 => init0(randomTile(g), x - 1)
      case _ => g
    }

    init0(Grid(size), startTiles)
  }

  private def randomTile(g: Grid): Grid =
    g.randomAvailable().map {
      pos =>
        Some(rand.nextInt(10))
          // one out of then is a 4
          .filter(_ == 0)
          .map(_ => Tile(pos, `4`))
          .getOrElse(Tile(pos, `2`))

    }.map(g + _).getOrElse(g)

  def apply(size: Int, startTiles: Int): Game = new Game(Settings(size, startTiles))
  def apply(root: Config): Game = new Game(Settings.fromConfig(root))

  /**
   * Value class for the Game's metadata
   * @param score     The current score of the game
   * @param gameOver  The game is over if there are no more moves available
   * @param won       Whether or not the game is won
   */
  case class GameData(score: Int, gameOver: Boolean, won: Boolean) {

    /**
     * @return true if the game is either won or definitely lost
     */
    def terminated: Boolean = gameOver || won
  }

  private[game] case class History(grid: Grid, move: Option[Move], score: Int)
}

/**
 * Settings for the Game
 *
 * @param size        the grid size
 * @param startTiles  start with that many tiles. Start tiles are chosen
 *                    between 2 (90%) and 4(10%)
 * @param underlying  a reference to the underlying config object, if provided
 */
case class Settings(size: Int, startTiles: Int, underlying: Option[Config] = None)
object Settings {
  def fromConfig(config: Config) = {
    config.checkValid(ConfigFactory.defaultReference(), "solver")

    Settings(
      config getInt "solver.grid-size",
      config getInt "solver.start-tiles",
      Some(config)
    )
  }
}
