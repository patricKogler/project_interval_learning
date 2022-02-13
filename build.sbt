ThisBuild / version := "0.1.0"

ThisBuild / scalaVersion := "3.1.1"

val circeVersion = "0.14.1"

lazy val core = (project in file("./core"))
  .settings(
    name := "project_intervall_learning",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "1.0.12",
      "dev.zio" %% "zio-test" % "1.0.12",
      "com.typesafe" % "config" % "1.4.1",
      "com.lihaoyi" %% "os-lib" % "0.8.1",
      "com.github.nscala-time" %% "nscala-time" % "2.30.0",
      "dev.zio" %% "zio-process" % "0.6.0",
      "org.scalatest" %% "scalatest" % "3.2.10" % "test",
      "org.scalacheck" %% "scalacheck" % "1.15.4" % "test"
    ),
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % circeVersion)
  )
