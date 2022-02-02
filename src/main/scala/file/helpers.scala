package file

import io.circe.jawn.decode
import io.circe.{Decoder, Encoder}
import os.Path
import entities.{Question, RawTopic, Topic, Topics}
import providers.path.PathProvider
import zio.{Has, Task, URIO, ZIO, ZLayer}
import zio.*

object helpers {

  object parse {
    sealed trait Parse {
      def parseQuestions: Task[List[RawTopic]]
    }

    case class ParseLive(pathProvider: PathProvider) extends Parse {

      private case class ParserHelper(mapping: Map[String, List[String]] = Map.empty, current: Option[String] = None) {
        def +(map: (String, List[String])): ParserHelper = this.copy(mapping = this.mapping + map)

        def setCurrent(curr: String): ParserHelper = this.copy(current = Some(curr))

        def map(f: ((String, List[String])) => (String, List[String])): ParserHelper = this.copy(mapping = mapping.map(f))
      }

      private def linesToRawTopics(lines: List[String]): List[RawTopic] =
        lines.foldLeft(ParserHelper())((helper, line) => {
          if line.startsWith("##") then
            val current = line.stripPrefix("##")
            helper.setCurrent(current) + (current -> helper.mapping.getOrElse(current, List.empty))
          else
            val lastOption = helper.current
            lastOption.map(k => helper.map(kv => if k == kv._1 then (kv._1, kv._2.appended(line)) else kv)).getOrElse(helper)
        }).mapping.map((k, v) => RawTopic(k, v)).toList

      override def parseQuestions: Task[List[RawTopic]] = pathProvider.getBasePath.map { baseDir =>
        os.walk(baseDir)
          .filter(_.last == "questions.txt")
          .map(os.read.lines)
          .map(_.toList.filter(!_.isBlank))
          .flatMap(str => linesToRawTopics(str)).toList
      }
    }

    object ParseLive {
      def layer: ZLayer[Has[PathProvider], Any, Has[Parse]] = ZLayer.fromService(ParseLive(_))
    }

    object Parse {
      def parseQuestions: ZIO[Has[Parse], Throwable, List[RawTopic]] = ZIO.serviceWith[Parse](_.parseQuestions)
    }
  }

}
