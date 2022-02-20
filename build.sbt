ThisBuild / version := "0.1.0"

ThisBuild / scalaVersion := "3.1.1"

lazy val shared = (project in file("./shared"))
  .settings(
    name := "shared",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.0.0-RC2",
      "dev.zio" %% "zio-test" % "2.0.0-RC2",
      "dev.zio" %% "zio-json" % "0.3.0-RC3",
      "com.github.nscala-time" %% "nscala-time" % "2.30.0",
    )
  )

lazy val core = (project in file("./core"))
  .settings(
    name := "core",
    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.4.2",
      "com.lihaoyi" %% "os-lib" % "0.8.1",
      "dev.zio" %% "zio-process" % "0.6.0",
      "org.scalatest" %% "scalatest" % "3.2.10" % "test",
      "org.scalacheck" %% "scalacheck" % "1.15.4" % "test"
    )
  ).dependsOn(shared)

