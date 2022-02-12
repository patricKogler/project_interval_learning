package file.helpers

import entities.{LectureConfig, RawLecture, RawTopic}
import io.circe.jawn.decode
import os.Path
import providers.path.PathProvider
import zio.{Has, Task, ZIO, ZLayer}

object parse {
  sealed trait Parse {
    def parseRawLectures: Task[List[RawLecture]]
  }

  case class ParseLive(pathProvider: PathProvider) extends Parse {

    import entities.LectureConfig.lectureConfigDecoder

    private case class ParserHelper(mapping: Map[String, List[String]] = Map.empty, current: Option[String] = None) {
      def +(map: (String, List[String])): ParserHelper = this.copy(mapping = this.mapping + map)

      def setCurrent(curr: String): ParserHelper = this.copy(current = Some(curr))

      def map(f: ((String, List[String])) => (String, List[String])): ParserHelper = this.copy(mapping = mapping.map(f))
    }

    private def findLectureFolders: ZIO[Any, Throwable, List[Path]] = pathProvider.getBasePath.map { baseDir =>
      os.walk(baseDir)
        .filter(_.last.matches("lecture.json"))
        .map(_ / os.up)
        .toList
    }

    private def getLectureConfigFromFolder(lectureFolder: Path): Task[LectureConfig] =
      ZIO.fromTry(decode[LectureConfig](os.read(lectureFolder / "lecture.json")).toTry)

    private def getQuestionFiles(path: Path): List[Path] =
      os.walk(path)
        .toList
        .filter(_.last.matches("questions.(md|txt)$"))


    private def parseRawTopics(lectureFolder: Path): Task[List[RawTopic]] = Task.foreach(os.walk(lectureFolder)
      .filter(_.last.matches("questions.(md|txt)$"))
      .map(os.read))(str => {
      if str.startsWith("#") then Task.succeed(str)
      else Task.fail(new Throwable(s"could not parse file in lecture Folder $lectureFolder expected # but got ${str.take(5)}..."))
    }).map { topicsAndQuestions =>
      linesToRawTopics(topicsAndQuestions.toList.flatMap(_.split("\n\n").toList.map(_.strip()).filter(_.nonEmpty)))
    }

    private def linesToRawTopics(lines: List[String]): List[RawTopic] =
      lines.foldLeft(ParserHelper())((helper, line) => {
        if line.startsWith("#") then
          val prefix = line.takeWhile(_ == '#')
          val current = line.stripPrefix(prefix)
          helper.setCurrent(current) + (current -> helper.mapping.getOrElse(current, List.empty))
        else
          val lastOption = helper.current
          lastOption.map(k => helper.map(kv => if k == kv._1 then (kv._1, kv._2.appended(line)) else kv)).getOrElse(helper)
      }).mapping.map((k, v) => RawTopic(k, v)).toList

    override def parseRawLectures: Task[List[RawLecture]] = findLectureFolders.flatMap { paths =>
      Task.foreach(paths) { path =>
        for {
          config <- getLectureConfigFromFolder(path)
          topics <- parseRawTopics(path)
        } yield RawLecture(config, topics)
      }
    }
  }

  object ParseLive {
    def layer: ZLayer[Has[PathProvider], Any, Has[Parse]] = ZLayer.fromService(ParseLive(_))
  }

  object Parse {
    def parseRawLectures: ZIO[Has[Parse], Throwable, List[RawLecture]] = ZIO.serviceWith[Parse](_.parseRawLectures)
  }
}
