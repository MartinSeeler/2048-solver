package de.chasmo.solver.game

import com.typesafe.config.ConfigFactory
import de.chasmo.solver.types.{Right, Up, Down, Left}
import de.chasmo.solver.util.GridUtils
import org.scalatest.{Matchers, FlatSpec}
import scala.collection.JavaConverters._

class GameSpec extends FlatSpec with Matchers {

  val config = ConfigFactory.parseMap(Map(
    "solver.grid-size" -> "4",
    "solver.start-tiles" -> "2"
  ).asJava)

  val game = Game(config)


  "The Game" should "correctly move right" in {
    val before = GridUtils.create(
      List(0, 0, 0, 2),
      List(0, 2, 8, 0),
      List(0, 4, 0, 8),
      List(2, 0, 0, 0))

    val after = List(
      List(0, 0, 0, 2),
      List(0, 0, 2, 8),
      List(0, 0, 4, 8),
      List(0, 0, 0, 2))

    val g = new Game(List(Game.History(before, None, 0)))

    GridUtils.cells(g.moveAround(Right, addRandomTile = false).grid) shouldBe after
  }

  it should "correctly move down" in {
    val before = GridUtils.create(
      List(0, 0, 0, 2),
      List(0, 2, 8, 0),
      List(0, 4, 0, 8),
      List(2, 0, 0, 0))

    val after = List(
      List(0, 0, 0, 0),
      List(0, 0, 0, 0),
      List(0, 2, 0, 2),
      List(2, 4, 8, 8))

    val g = new Game(List(Game.History(before, None, 0)))

    GridUtils.cells(g.moveAround(Down, addRandomTile = false).grid) shouldBe after
  }

  it should "correctly move left" in {
    val before = GridUtils.create(
      List(0, 0, 0, 2),
      List(0, 2, 8, 0),
      List(0, 4, 0, 8),
      List(2, 0, 0, 0))

    val after = List(
      List(2, 0, 0, 0),
      List(2, 8, 0, 0),
      List(4, 8, 0, 0),
      List(2, 0, 0, 0))

    val g = new Game(List(Game.History(before, None, 0)))

    GridUtils.cells(g.moveAround(Left, addRandomTile = false).grid) shouldBe after
  }

  it should "correctly move up" in {
    val before = GridUtils.create(
      List(0, 0, 0, 2),
      List(0, 2, 8, 0),
      List(0, 4, 0, 8),
      List(2, 0, 0, 0))

    val after = List(
      List(2, 2, 8, 2),
      List(0, 4, 0, 8),
      List(0, 0, 0, 0),
      List(0, 0, 0, 0))

    val g = new Game(List(Game.History(before, None, 0)))

    GridUtils.cells(g.moveAround(Up, addRandomTile = false).grid) shouldBe after
  }

  it should "merge correctly when moving right" in {
    val before = GridUtils.create(
      List(0, 2, 0, 0),
      List(0, 8, 8, 0),
      List(0, 4, 0, 4),
      List(2, 2, 2, 0))

    val after = List(
      List(0, 0, 0, 2),
      List(0, 0, 0, 16),
      List(0, 0, 0, 8),
      List(0, 0, 2, 4))

    val g = new Game(List(Game.History(before, None, 0)))

    GridUtils.cells(g.moveAround(Right, addRandomTile = false).grid) shouldBe after
  }

  it should "merge correctly when moving down" in {
    val before = GridUtils.create(
      List(0, 0, 0, 0),
      List(2, 4, 8, 2),
      List(2, 0, 8, 0),
      List(2, 4, 0, 0))

    val after = List(
      List(0, 0, 0, 0),
      List(0, 0, 0, 0),
      List(2, 0, 0, 0),
      List(4, 8, 16, 2))

    val g = new Game(List(Game.History(before, None, 0)))

    GridUtils.cells(g.moveAround(Down, addRandomTile = false).grid) shouldBe after
  }

  it should "merge correctly when moving left" in {
    val before = GridUtils.create(
      List(0, 2, 0, 0),
      List(0, 8, 8, 0),
      List(0, 4, 0, 4),
      List(2, 2, 2, 0))

    val after = List(
      List(2, 0, 0, 0),
      List(16, 0, 0, 0),
      List(8, 0, 0, 0),
      List(4, 2, 0, 0))

    val g = new Game(List(Game.History(before, None, 0)))

    GridUtils.cells(g.moveAround(Left, addRandomTile = false).grid) shouldBe after
  }

  it should "merge correctly when moving up" in {
    val before = GridUtils.create(
      List(0, 0, 0, 0),
      List(2, 4, 8, 2),
      List(2, 0, 8, 0),
      List(2, 4, 0, 0))

    val after = List(
      List(4, 8, 16, 2),
      List(2, 0, 0, 0),
      List(0, 0, 0, 0),
      List(0, 0, 0, 0))

    val g = new Game(List(Game.History(before, None, 0)))

    GridUtils.cells(g.moveAround(Up, addRandomTile = false).grid) shouldBe after
  }
}
