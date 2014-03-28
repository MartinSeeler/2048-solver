package de.chasmo.solver
package game

import Game.GameData
import types._

import scala.concurrent.{ExecutionContext, Future}


/**
 * Cake Component for the Decision made by the player of the game.
 */
trait DecisionComponent {

  def decider: Decider

  trait Decider {

    /**
     * Responsible for deciding the next next action.
     * Since this is naturally a blocking operation, this an async operation
     * @param grid      the current grid
     * @param data      metadata about the game
     * @param settings  the game's settings
     * @param ec    the implicit ExecutionContext for the Future
     * @return      A Future, eventually holding the action made by the user
     */
    def decide(grid: Grid, data: GameData, settings: Settings)(implicit ec: ExecutionContext): Future[Action]
  }


  sealed trait Action
  case object Undo extends Action
  case object Quit extends Action
  case object Reset extends Action
  case class Play(m: Move) extends Action
}



