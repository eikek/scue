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
  val slf4j = "1.6.4"
  val logback = "1.0.1"
  val scalaTest = "2.0.M4"
  val grizzled = "0.6.9"
  val scala = "2.9.2"
  val blueprints = "2.1.0"
}

object Dependencies {

  val slf4jApi = "org.slf4j" % "slf4j-api" % Version.slf4j % "provided"
  val scalaTest = "org.scalatest" %% "scalatest" % Version.scalaTest % "test" withSources()
  val slf4jSimple = "org.slf4j" % "slf4j-simple" % Version.slf4j % "test"
  val blueprintsNeo4j = "com.tinkerpop.blueprints" % "blueprints-neo4j-graph" % Version.blueprints % "test" withSources()
  val blueprintsOrient = "com.tinkerpop.blueprints" % "blueprints-orient-graph" % Version.blueprints % "test" withSources()
  val blueprintsCore = "com.tinkerpop.blueprints" % "blueprints-core" % Version.blueprints % "provided" withSources()

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
    version := "1.0.0-SNAPSHOT",
    organization := "org.eknet.scue",
    scalaVersion := Version.scala,
    exportJars := true,
    scalacOptions ++= Seq("-unchecked", "-deprecation"),
    publishMavenStyle := true,
    publishTo := Some("eknet-maven2" at "https://eknet.org/maven2"),
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    pomIncludeRepository := (_ => false),
    pomExtra := <licenses>
      <license>
        <name>Apache 2</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <connection>scm:git:https://eknet.org/git/scue.git</connection>
      <url>https://eknet.org/gitr/?r=scue.git</url>
    </scm>
  )

  val deps = Seq(slf4jApi, blueprintsCore, scalaTest, blueprintsOrient, blueprintsNeo4j, slf4jSimple)
}


