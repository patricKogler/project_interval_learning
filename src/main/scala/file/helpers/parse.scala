package file.helpers
import entities.*
import os.Path
import providers.path.PathProvider
import zio.*
import zio.json.*

object parse {
  sealed trait Parse {
    def parseRawLectures: IO[String, List[RawLecture]]
  }

  case class ParseLive(pathProvider: PathProvider) extends Parse {

    private case class ParserHelper(mapping: Map[String, List[String]] = Map.empty, current: Option[String] = None) {
      def +(map: (String, List[String])): ParserHelper = this.copy(mapping = this.mapping + map)

      def setCurrent(curr: String): ParserHelper = this.copy(current = Some(curr))

      def map(f: ((String, List[String])) => (String, List[String])): ParserHelper = this.copy(mapping = mapping.map(f))
    }

    private def findLectureFolders: IO[String, List[Path]] = pathProvider.getBasePath.map { baseDir =>
      os.walk(baseDir)
        .filter(_.last.matches("lecture.json"))
        .map(_ / os.up)
        .toList
    }

    private def getLectureConfigFromFolder(lectureFolder: Path): IO[String, LectureConfig] =
      ZIO.fromEither(os.read(lectureFolder / "lecture.json").fromJson[LectureConfig])

    private def getQuestionFiles(path: Path): List[Path] =
      os.walk(path)
        .toList
        .filter(_.last.matches("questions.(md|txt)$"))


    private def parseRawTopics(lectureFolder: Path): IO[String, List[RawTopic]] = IO.foreach(os.walk(lectureFolder)
      .filter(_.last.matches("questions.(md|txt)$"))
      .map(os.read))(str => {
      if str.startsWith("#") then Task.succeed(str)
      else IO.fail(s"could not parse file in lecture Folder $lectureFolder expected # but got ${str.take(5)}...")
    }).map { topicsAndQuestions =>
      linesToRawTopics(topicsAndQuestions.toList.flatMap(_.split("\n\n").toList.map(_.strip()).filter(_.nonEmpty)))
    }

    private def linesToRawTopics(lines: List[String]): List[RawTopic] =
      lines.foldLeft(ParserHelper())((helper, line) => {
        if line.startsWith("#") then
          val prefix = line.takeWhile(_ == '#')
          val current = line.stripPrefix(prefix).strip()
          helper.setCurrent(current) + (current -> helper.mapping.getOrElse(current, List.empty))
        else
          val lastOption = helper.current
          lastOption.map(k => helper.map(kv => if k == kv._1 then (kv._1, kv._2.appended(line)) else kv)).getOrElse(helper)
      }).mapping.map((k, v) => RawTopic(k, v)).toList

    override def parseRawLectures: IO[String, List[RawLecture]] = findLectureFolders.flatMap { paths =>
      IO.foreach(paths) { path =>
        for {
          config <- getLectureConfigFromFolder(path)
          topics <- parseRawTopics(path)
        } yield RawLecture(config, topics)
      }
    }
  }

  object ParseLive {
    def layer: URLayer[PathProvider, Parse] = (ParseLive(_)).toLayer[Parse]
  }

  object Parse {
    def parseRawLectures: ZIO[Parse, String, List[RawLecture]] = ZIO.serviceWithZIO[Parse](_.parseRawLectures)
  }
}
