import sbt._
import sbt.Keys._

object DelsymBuild extends Build {

  lazy val delsym = Project(
    id = "delsym",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "delsym",
      organization := "com.mycompany",
      version := "0.1-SNAPSHOT",
      scalaVersion := "2.10.2"
      // add other settings here
    )
  )
}
