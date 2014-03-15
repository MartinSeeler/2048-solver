package de.chasmo.solver

import com.typesafe.config.ConfigFactory
import de.chasmo.solver.game.{MediatorComponent, Game}
import de.chasmo.solver.types._
import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Await}
import scala.concurrent.duration.Duration


class GameSolver {
  this: MediatorComponent =>

  import GameSolver._

  val config = ConfigFactory.load()
  def startGame = Game(config)

  @tailrec private def loop(game: Game)(implicit ec: ExecutionContext): Game = {

    mediator.paint(game.grid, game.lastMove, game.data)

    val action = Await.result(mediator.action, Duration.Inf)
    action match {
      case Quit       => game
      case Reset      => loop(startGame)
      case Undo       => loop(game.undo)
      case Play(move) => loop(game.move(move))
    }
  }

  def run(implicit ec: ExecutionContext): Unit = {
    val result = loop(startGame)

    mediator.result(result.score, result.data)
  }
}

object GameSolver {
  sealed trait Action
  case object Undo extends Action
  case object Quit extends Action
  case object Reset extends Action
  case class Play(m: Move) extends Action
}


object ConsoleGameSolver extends GameSolver with MediatorComponent {
  def mediator = new ConsoleMediator

  def main(args: Array[String]) {
    import scala.concurrent.ExecutionContext.Implicits.global

    run
  }
}
