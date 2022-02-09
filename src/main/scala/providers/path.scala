package providers

import com.typesafe.config.ConfigFactory
import os.Path
import zio.{Has, Task, ULayer, ZLayer}

object path {
  trait PathProvider {
    def getBasePath: Task[Path]

    def getQuestionsDir: Task[Path]

    def getLecturesFile: Task[Path]
  }

  case class PathProviderLive() extends PathProvider {

    private lazy val baseDir = os.Path(ConfigFactory.load().getString("basedir"));
    private lazy val questionsDir = baseDir / "_questions"
    private lazy val lecturesFile = questionsDir / "lectures.json"

    override def getBasePath: Task[Path] = Task.effect(baseDir)

    override def getQuestionsDir: Task[Path] = for {
      basePath <- getBasePath
      qdir <- Task.effect {
        if !os.exists(questionsDir) then os.makeDir(questionsDir)
        questionsDir
      }
    } yield qdir

    override def getLecturesFile: Task[Path] = for {
      _ <- getQuestionsDir
      topics <- Task.effect {
        if os.exists(lecturesFile) then lecturesFile
        else
          os.write(lecturesFile, "")
          lecturesFile
      }
    } yield topics
  }

  object PathProviderLive {
    def layer: ULayer[Has[PathProvider]] = ZLayer.succeed(PathProviderLive())
  }
}
