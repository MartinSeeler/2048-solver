package de.chasmo.solver
package game

import Game.GameData
import types.{Grid, Move}

/**
 * Cake Component for the Painter, that updates the UI, whatever the UI actually is...
 */
trait PaintComponent {

  def painter: Painter

  trait Painter {

    /**
     * Responsible for displaying the current state to the user
     * @param grid      the current grid
     * @param lastMove  the last move made, if available
     * @param data      metadata about the game
     */
    def paint(grid: Grid, lastMove: Option[Move], data: GameData): Unit

    /**
     * Notification, when the game is over
     * @param score the achieved score
     * @param data  the final meta data
     */
    def result(score: Int, data: GameData): Unit
  }
}



