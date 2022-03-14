package providers

import com.typesafe.config.ConfigFactory
import os.Path
import zio.*

object path {
  trait PathProvider {
    def getBasePath: IO[String, Path]

    def getQuestionsDir: IO[String, Path]

    def getLecturesFile: IO[String, Path]
  }

  case class PathProviderLive() extends PathProvider {

    private lazy val baseDir = os.Path(ConfigFactory.load().getString("basedir"));
    private lazy val questionsDir = baseDir / "_questions"
    private lazy val lecturesFile = questionsDir / "lectures.json"

    override def getBasePath: IO[String, Path] = IO.attempt(baseDir).mapError(_.toString)

    override def getQuestionsDir: IO[String, Path] = for {
      basePath <- getBasePath
      qdir <- IO.attempt {
        if !os.exists(questionsDir) then os.makeDir(questionsDir)
        questionsDir
      }.mapError(_.toString)
    } yield qdir

    override def getLecturesFile: IO[String, Path] = for {
      _ <- getQuestionsDir
      topics <- Task.attempt {
        if os.exists(lecturesFile) then lecturesFile
        else
          os.write(lecturesFile, "")
          lecturesFile
      }.mapError(_.toString)
    } yield topics
  }

  object PathProviderLive {
    def layer: ULayer[PathProvider] = ZLayer.succeed(PathProviderLive())
  }
}
