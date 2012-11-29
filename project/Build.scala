/*
 * Copyright 2012 Eike Kettner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import sbt._
import Keys._
import Dependencies._

object Version {
  val slf4j = "1.7.2"
  val logback = "1.0.7"
  val scalaTest = "2.0.M4"
  val grizzled = "0.6.9"
  val scala = "2.9.2"
  val blueprints = "2.1.0"
  val titan = "0.1.0"
  val guava = "13.0.1"
  val neoswing = "2.0.0-m1"
}

object Dependencies {

  val slf4jApi = "org.slf4j" % "slf4j-api" % Version.slf4j % "provided"
  val blueprintsCore = "com.tinkerpop.blueprints" % "blueprints-core" % Version.blueprints % "provided" withSources()

  val testDeps = Seq(
    "org.scalatest" %% "scalatest" % Version.scalaTest,
    "org.slf4j" % "slf4j-simple" % Version.slf4j,
    "com.tinkerpop.blueprints" % "blueprints-orient-graph" % Version.blueprints exclude("com.tinkerpop.blueprints", "blueprints-core") exclude("org.slf4j", "slf4j-log4j12") withSources(),
    "com.thinkaurelius.titan" % "titan" % Version.titan exclude("com.tinkerpop.blueprints", "blueprints-core") exclude("org.slf4j", "slf4j-log4j12") withSources(),
    "com.google.guava" % "guava" % Version.guava,
    "org.eknet.neoswing" % "neoswing" % Version.neoswing exclude("ch.qos.logback", "logback-classic")
  ) map(_ % "test")
}

// Root Module 

object RootBuild extends Build {

  lazy val root = Project(
    id = "scue",
    base = file("."),
    settings = buildSettings
  ) 

  val buildSettings = Project.defaultSettings ++ Seq(
    name := "scue",
    libraryDependencies ++= deps
  )

  override lazy val settings = super.settings ++ Seq(
    version := "0.1.0-SNAPSHOT",
    organization := "org.eknet.scue",
    licenses := Seq(("ASL2", new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))),
    scmInfo := Some(ScmInfo(new URL("https://eknet.org/gitr/?r=scue.git"), "scm:git:https://eknet.org/git/scue.git")),
    scalaVersion := Version.scala,
    exportJars := true,
    scalacOptions ++= Seq("-unchecked", "-deprecation"),
    publishMavenStyle := true,
    publishTo := Some("eknet-maven2" at "https://eknet.org/maven2"),
    publishArtifact in Test := true,
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    pomIncludeRepository := (_ => false)
  )

  val deps = Seq(slf4jApi, blueprintsCore) ++ testDeps
}


