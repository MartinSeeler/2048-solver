package de.chasmo.solver
package game

import types.{Move, Num, Grid}

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.collection.immutable.SortedMap

import com.typesafe.config.{ConfigFactory, Config}


/**
 * The effing addicting 2048 game
 *
 * Each instance of this class represents a given state.
 * The Game is immutable and persistent, one could therefore easily jump
 * back and forth between different states
 */
class Game private[game](history: List[Game.History], val settings: Settings) extends GameLike {
  import Game.History

  /**
   * The primary (public) constructor
   * @param settings  the settings of this Game, see [[Settings]]
   * @return The Game, motherfucker, do you play it?   http://goo.gl/gmgo0o
   */
  def this(settings: Settings) = this(Game.History.init(settings.size, settings.startTiles, settings.rates, settings.random), settings)

  import Game.GameData

  val data = {
    val gameOver = !hasMoves
    val won = isWin(grid)
    val terminated = !hasMoves || (won && !settings.keepPlaying)

    GameData(score, gameOver, won, terminated)
  }

  /**
   * @return the current grid
   */
  implicit lazy val grid = history.head.grid

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
   * @return a new game with an updated grid and history.
   *         If no move was possible, return this
   */
  def move(move: Move): Game = {
    if (data.terminated) this
    else moveAround(move, score) match {
      case Some(h @ History(newGrid, _, _)) =>
        val hist = randomTile(newGrid, settings.rates, settings.random).map(t => h.copy(grid = newGrid + t)).getOrElse(h)
        new Game(hist :: history, settings)
      case None => this
    }
  }

  /**
   * go back one state
   * @return a new game, representing the previous state.
   *         If there is no previous action, return this
   */
  def undo = history match {
    case current :: previous :: rest =>
      new Game(previous :: rest, settings)
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
}

object Game {

  def apply(root: Config): Game = new Game(Settings(root))

  /**
   * Value class for the Game's metadata
   * @param score      The current score of the game
   * @param gameOver   The game is over if there are no more moves available
   * @param won        Whether or not the game is won
   * @param terminated Whether or not the game is terminated (i.e. moves are accepted)
   */
  case class GameData(score: Int, gameOver: Boolean, won: Boolean, terminated: Boolean)
  object GameData {
    val empty = GameData(0, gameOver = false, won = false, terminated = false)
  }

  /**
   * Value class for the Game's History
   * @param grid  The grid associated with the history
   * @param move  the move, that lead to the history
   * @param score the score from the history
   */
  case class History(grid: Grid, move: Option[Move], score: Int)
  object History {
    def init(size: Int, startTiles: Int, rates: List[(Double, Num)], random: Boolean) = {
      val initial = apply(GameLike.initGrid(size, startTiles, rates, random), None, 0)
      List(initial)
    }
  }
}

/**
 * Settings for the Game
 *
 * @param size        the grid size
 * @param startTiles  start with that many tiles. Start tiles are chosen
 *                    between 2 (90%) and 4(10%)
 * @param rates       a list of chances ((0,1)) and the corresponding tile, that
 *                    be spawned at the given chance
 * @param keepPlaying If true, do not terminated when the game is won
 * @param underlying  The underlying config object
 */
case class Settings(size: Int, startTiles: Int, rates: List[(Double, Num)], random: Boolean, keepPlaying: Boolean, underlying: Config)

object Settings {
  def apply(config: Config): Settings = {
    config.checkValid(ConfigFactory.defaultReference(), "solver")
    config.checkValid(ConfigFactory.defaultReference(), "solver.spawn")

    val spawn = config.getConfig("solver.spawn")
    val tiles = spawn.getIntList("tiles").asScala
    val chances = spawn.getConfig("percentages")

    @tailrec def loop0(rates: Map[Int, Num], total: Int, remaining: List[Int]): Map[Int, Num] = remaining match {
      case n :: rest =>
        Num.lift(n) match {
          case Some(num) if chances.hasPath(n.toString) =>
            val chance = chances getInt n.toString
            val actual = total - chance
            if (actual < 0) rates
            else loop0(rates + (actual -> num), actual, rest)
          case _ => loop0(rates, total, rest)
        }
      case _ => rates
    }

    val rates = loop0(SortedMap.empty(implicitly[Ordering[Int]].reverse), 100, tiles.map(_.toInt).toList)

    Settings(
      config getInt "solver.grid-size",
      config getInt "solver.start-tiles",
      rates.map{case (r, n) => (r / 100.0) -> n}.toList,
      spawn getBoolean "random",
      config getBoolean "solver.keep-playing",
      config
    )
  }
}
