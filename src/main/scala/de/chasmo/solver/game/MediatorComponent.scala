package de.chasmo.solver.game

import de.chasmo.solver.GameSolver.Action
import de.chasmo.solver.game.Game.GameData
import de.chasmo.solver.types._
import java.io.PrintStream
import scala.Some
import scala.concurrent.{ExecutionContext, Future, blocking}

/**
 * Cake Component for the Mediator that talks between user and game
 */
trait MediatorComponent {

  def mediator: Mediator

  trait Mediator {

    /**
     * Responsible for displaying the current state to the user
     * @param grid      the current grid
     * @param lastMove  the last move made, if available
     * @param data      metadata about the game
     */
    def paint(grid: Grid, lastMove: Option[Move], data: GameData): Unit

    /**
     * Responsible for asking the user about their next action.
     * Since this is naturally a blocking operation, this a a async operation
     * @param ec    the implicit [[ExecutionContext]] for the Future
     * @return      A Future, eventually holding the action made by the user
     */
    def action(implicit ec: ExecutionContext): Future[Action]

    /**
     * Notification, when the game is over
     * @param score the achieved score
     * @param data  the final meta data
     */
    def result(score: Int, data: GameData): Unit
  }


  /**
   * Play the game using the Console
   *
   * Controls:
   * - Arrows
   * - WASD
   * - vim (hjkl)
   *
   * Meta:
   * - space restarts the game
   * - enter or q quit the game
   */
  class ConsoleMediator(out: PrintStream = System.out) extends Mediator {

    import de.chasmo.solver.GameSolver._
    import de.chasmo.solver.types.{Right, Down, Left, Up}

    private val con = new tools.jline.console.ConsoleReader

    private val keyMap: Map[Int, Action] = Map(

      6 -> Play(Right), // arrows
      'd'.toInt -> Play(Right), // WASD
      'l'.toInt -> Play(Right), // vim

      14 -> Play(Down), // arrows
      's'.toInt -> Play(Down), // WASD
      'j'.toInt -> Play(Down), // vim

      2 -> Play(Left), // arrows
      'a'.toInt -> Play(Left), // WASD
      'h'.toInt -> Play(Left), // vim

      16 -> Play(Up), // arrows
      'w'.toInt -> Play(Up), // WASD
      'k'.toInt -> Play(Up), // vim

      8 -> Undo, // backspace
      'u'.toInt -> Undo, // u

      32 -> Reset, // space
      10 -> Quit, // return
      'q'.toInt -> Quit // q
    )

    private val colorMap = {
      val m = Map[Num, String](
        `2048` -> Console.YELLOW_B,
        `1024` -> Console.RED_B,
        `512` -> Console.GREEN_B,
        `256` -> Console.MAGENTA_B,
        `128` -> Console.BLUE,
        `64` -> Console.MAGENTA,
        `32` -> Console.CYAN,
        `16` -> Console.GREEN,
        `8` -> Console.RED,
        `4` -> Console.YELLOW
      )
      m withDefaultValue Console.WHITE
    }

    private val validChars = keyMap.keys.map(_.toChar).toList

    /**
     * print a wonderful ASCII table of the grid
     */
    def paint(grid: Grid, lastMove: Option[Move], data: GameData): Unit = {

      val width = 6

      val border = List.fill(grid.size)("-" * width + "--").mkString("+", "|", "+")
      val row = List.fill(grid.size)(s" %${width}s ").mkString("+", "|", "+")
      val footer = s"${"score".padTo(width + 3, ' ')}: ${data.score}  --  last move: ${lastMove.getOrElse("n/a")}"

      val table = Seq(
        Seq(border),
        Seq.fill(grid.size)(row),
        Seq(border, footer)
      ).flatten

      val layout = table.mkString("\n") format (printCells(grid): _*)

      out.println(layout)
    }

    def action(implicit ec: ExecutionContext) = {

      Future {
        blocking {
          val key = con.readCharacter(validChars: _*)
          keyMap(key)
        }
      }
    }

    def result(score: Int, data: GameData) = {
      out.println(s"final score: $score")
      if (data.won) {
        out.println(s"wow! such 2048, much win!")
      }
    }

    /**
     * @return a string list of color-printed tiles. The order is row-wise from
     *         top-left to bottom-right
     */
    private def printCells(g: Grid) = {
      val allPositions = (0 until g.size).flatMap {
        y =>
          (0 until g.size).map(Pos(_, y))
      }

      val printed = allPositions.foldLeft(List.empty[String]) {
        (strings, pos) =>
          val s = g(pos) match {
            case Some(tile) => colorMap(tile.num) + tile.num + Console.RESET
            case None       => "[    ]"
          }
          s :: strings
      }

      printed.reverse
    }
  }

}



