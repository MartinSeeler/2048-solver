import spray.revolver.RevolverPlugin.Revolver

name := "2048-solver"

organization := "de.chasmo"

version := "1.0-SNAPSHOT"

scalaVersion := "2.10.4"

scalacOptions ++= Seq(
  "-feature",
  "-unchecked",
  "-deprecation",
  "-language:reflectiveCalls",
  "-Xlint",
  "-Ywarn-dead-code",
  "-language:_",
  "-target:jvm-1.7",
  "-encoding", "UTF-8")

libraryDependencies ++= Seq(
  "org.scala-lang"      %  "jline"       % "2.10.4"   exclude ("org.fusesource.jansi", "jansi"),
  "com.typesafe"        %  "config"      % "1.2.0",
  "com.netflix.rxjava"  % "rxjava-scala" % "0.17.1",
  "org.scalatest"      %%  "scalatest"   % "2.1.0"    % "test",
  "org.scalacheck"     %%  "scalacheck"  % "1.11.3"   % "test"
)

mainClass in Compile := Some("de.chasmo.solver.SimpleBotSolver")

licenses += "The MIT License" -> url("http://knutwalker.mit-license.org/license.txt")

scmInfo := Some(ScmInfo(url("https://github.com/MartinSeeler/2048-solver"), "scm:git:https://github.com/MartinSeeler/2048-solver.git", Some("scm:git:ssh://git@github.com:MartinSeeler/2048-solver.git")))

pomExtra :=
  <developers>
    <developer>
      <id>chasmo</id>
      <name>Martin Seeler</name>
    </developer>
    <developer>
      <id>knutwalker</id>
      <name>Paul Horn</name>
    </developer>
  </developers>

net.virtualvoid.sbt.graph.Plugin.graphSettings

Revolver.settings
