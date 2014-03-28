package de.chasmo.solver
package game

import com.typesafe.config.ConfigFactory
import scala.annotation.tailrec
import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration.Duration
import java.util.concurrent.Executors

/**
 * A Solver combines a [[PaintComponent]] and a [[DecisionComponent]]
 * with some Boilerplate game logic
 */
trait Solver {
  this: PaintComponent with DecisionComponent =>

  val config = ConfigFactory.load()
  val settings = Settings(config)
  def startGame = new Game(settings)

  @tailrec private def loop(game: Game)(implicit ec: ExecutionContext): Game = {

    painter.paint(game.grid, game.lastMove, game.data)

    // OK, we block here, but, what the heck...
    val action = Await.result(decider.decide(game.grid, game.data, game.settings), Duration.Inf)
    action match {
      case Quit       => game
      case Reset      => loop(startGame)
      case Undo       => loop(game.undo)
      case Play(move) => loop(game.move(move))
    }
  }

  def run(implicit ec: ExecutionContext): Unit = {
    val result = loop(startGame)

    painter.result(result.score, result.data)
  }

  def main(args: Array[String]) {
//    import scala.concurrent.ExecutionContext.Implicits.global

    implicit val ec = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(64))

    run

    ec.shutdown()
  }

}
