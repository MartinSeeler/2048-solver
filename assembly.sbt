import sbtassembly.Plugin._
import AssemblyKeys._

assemblySettings

jarName in assembly := s"${name.value}.jar"

outputPath in assembly := baseDirectory.value / (jarName in assembly).value

test in assembly := {}

mainClass in assembly := Some("de.chasmo.solver.ConsoleGameSolver")

assemblyOption in assembly ~= { _.copy(prependShellScript = Some(defaultShellScript)) }

