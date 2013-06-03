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

object Version {
  val slf4j = "1.7.2"
  val scala = "2.10.1"
  val blueprints = "2.3.0"
  val titan = "0.3.1"
  val neoswing = "2.0.0-m1"

  val scalaTest = "1.9.1"

}

object Dependencies {

  val slf4jApi = "org.slf4j" % "slf4j-api" % Version.slf4j % "provided"
  val blueprintsCore = "com.tinkerpop.blueprints" % "blueprints-core" % Version.blueprints % "provided"

  val testDeps = Seq(
    "org.scalatest" %% "scalatest" % Version.scalaTest,
    "org.slf4j" % "slf4j-simple" % Version.slf4j,
    "com.tinkerpop.blueprints" % "blueprints-orient-graph" % Version.blueprints exclude("com.tinkerpop.blueprints", "blueprints-core") exclude("org.slf4j", "slf4j-log4j12"),
    "com.thinkaurelius.titan" % "titan-core" % Version.titan exclude("com.tinkerpop.blueprints", "blueprints-core") exclude("org.slf4j", "slf4j-log4j12"),
    "com.thinkaurelius.titan" % "titan-berkeleyje" % Version.titan exclude("com.tinkerpop.blueprints", "blueprints-core") exclude("org.slf4j", "slf4j-log4j12")
//    "org.eknet.neoswing" % "neoswing" % Version.neoswing exclude("com.tinkerpop.blueprints", "blueprints-core") exclude("ch.qos.logback", "logback-classic")
  ) map(_ % "test")
}

// Root Module 

object Build extends sbt.Build {
  import Dependencies._

  lazy val root = Project(
    id = "scue",
    base = file("."),
    settings = buildSettings
  )

  val buildSettings = Project.defaultSettings ++ Seq(
    name := "scue",
    libraryDependencies ++= Seq(slf4jApi, blueprintsCore) ++ testDeps
  )

  override lazy val settings = super.settings ++ Seq(
    version := "0.3.0-SNAPSHOT",
    organization := "org.eknet.scue",
    licenses := Seq(("ASL2", new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))),
    scmInfo := Some(ScmInfo(new URL("https://eknet.org/gitr/?r=scue.git"), "scm:git:https://eknet.org/git/scue.git")),
    scalaVersion := Version.scala,
    exportJars := true,
    resolvers ++= Seq("eknet.org" at "https://eknet.org/maven2", "oracle.com" at "http://download.oracle.com/maven"),
    scalacOptions ++= Seq("-unchecked", "-deprecation"),
    publishMavenStyle := true,
    publishTo := Some("eknet-maven2" at "https://eknet.org/maven2"),
    publishArtifact in Test := true,
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    crossScalaVersions := Seq("2.9.2", "2.9.3", "2.10.1"),
    pomIncludeRepository := (_ => false)
  )

}


