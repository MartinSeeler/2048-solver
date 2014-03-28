package de.chasmo.solver

import game.Solver
import impl.{HumanConsolePlayer, ConsolePainter, SimpleBot, SummarizePainter}


object HumanSolver extends Solver with ConsolePainter with HumanConsolePlayer
object SimpleBotSolver extends Solver with ConsolePainter with SimpleBot
object SimpleBotSummarySolver extends Solver with SummarizePainter with SimpleBot
