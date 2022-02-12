package services.topic

import entities.{Lecture, LectureConfig, Lectures, Question, Topic}
import helpers.question.getQuestionWeight
import zio.{Has, Task, ZIO, ZLayer}
import zio.console.{Console, putStrLn}

import java.util.UUID
import scala.annotation.tailrec
import scala.io.AnsiColor
import scala.io.AnsiColor.*

object selection {
  trait UserSelection {
    def getSelection(lectures: Lectures): Task[Lectures]
  }

  case class UserSelectionLive(console: Console.Service) extends UserSelection {

    private type LectureList = List[(String, UUID, List[(String, UUID)])]

    private def lecturesToList(lectures: Lectures): Task[LectureList] = {
      Task.succeed(lectures.lectures.foldLeft(List.empty) { (list, lecture) =>
        list.appended((s"${lecture.lectureConfig.name} (${lecture.topics.map(_.getAllQuestions.length).sum})", lecture.id, lecture.topics.foldLeft(List.empty) { (topicList, topic) =>
          topicList.appended((s"${topic.name} (${topic.questions.length})", topic.id))
        }))
      })
    }

    def getColorByQuestionWeight(weight: Double): String = {
      if weight > 3.8 then return RED_B
      if weight > 1.6 then YELLOW_B
      else GREEN_B
    }

    private def lecturesToString(lectures: Lectures): String = {
      def topicsHelper(stringBuilder: StringBuilder, topics: List[Topic], index: String, topicIndex: Int, lectureConfig: LectureConfig): String = {
        topics match {
          case ::(head, next) => topicsHelper(
            stringBuilder.append(s"   ${getColorByQuestionWeight(head.getAllQuestions.map(q => getQuestionWeight(q, lectureConfig)).sum / head.getAllQuestions.length)}$index.$topicIndex)${RESET} ${head.name} (${head.getAllQuestions.length})\n").append(topicsHelper(
              new StringBuilder, head.subTopics, s"   $index.$topicIndex", 0, lectureConfig
            )),
            next,
            index,
            topicIndex + 1, lectureConfig)
          case Nil => stringBuilder.toString()
        }
      }

      @tailrec
      def lectureHelper(stringBuilder: StringBuilder, lectureList: List[Lecture], index: Int): String = {
        lectureList match {
          case ::(head, next) => lectureHelper(
            stringBuilder.append(s"$index) ${head.lectureConfig.name} (${head.topics.map(_.getAllQuestions.length).sum})\n").append(topicsHelper(new StringBuilder, head.topics, index.toString, 0, head.lectureConfig)),
            next,
            index + 1)
          case Nil => stringBuilder.toString()
        }
      }

      lectureHelper(new StringBuilder, lectures.lectures, 0)
    }

    def filterBySelectionString(selectionString: String, lectures: Lectures): List[Lecture | Topic] = {
      if selectionString.contains('a') then lectures.lectures
      else {
        // work with regex because maybe sub sub sub... index eg 1.2.3.4.1
        val allIndexes = selectionString.split(",").map(_.strip).filter(_.nonEmpty).filter(_.matches("\\d+(\\.\\d+)*")).distinct

        // remove all that are covered by parent eg if 1 then remove 1.2
        // [1.2, 1, 0.0, 0.0.1] => [1, 0.0]
        val allRelevantIndexes = allIndexes.filter { index =>
          val current = index.split("\\.")
          !allIndexes.filter(_ != index).exists { other =>
            val strings = other.split("\\.")
            if strings.length >= current.length then false
            else strings.zip(current).forall(_ == _)
          }
        }
        allRelevantIndexes.map(index => lectures.getByIndex(index)).filter(_.nonEmpty).map(_.get).toList
      }
    }

    override def getSelection(lectures: Lectures): Task[Lectures] = for {
      _ <- console.putStrLn("select by typing either \"a\" for all or 0, 1.2, 1.1, .... for specific topics or whole lectures")
      s = lecturesToString(lectures)
      _ <- console.putStrLn(s)
      selection <- console.getStrLn.map(s => filterBySelectionString(s, lectures))
      _ <- console.putStrLn(selection.toString())
    } yield lectures.filterByQuestion((q, _) => selection.flatMap({
      case lecture: Lecture => lecture.topics.flatMap(_.getAllQuestions)
      case topic: Topic => topic.getAllQuestions
    }).exists(_.id == q.id))
  }

  object UserSelectionLive {
    def layer: ZLayer[Console, Throwable, Has[UserSelection]] = ZLayer.fromService(UserSelectionLive(_))
  }

  object UserSelection {
    def getSelection(lectures: Lectures) = ZIO.serviceWith[UserSelection](_.getSelection(lectures))
  }
}
