package entities

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import Lecture.{lectureDecoder, lectureEncoder}

import scala.annotation.tailrec
import scala.util.Try

case class Lectures(lectures: List[Lecture] = List.empty) {

  def getByIndex(index: String): Option[Lecture | Topic] =
    if index.matches("\\d+(\\.\\d+)*") then {
      val indexes = index.split("\\.").map(_.toInt)
      if indexes.length == 1 then lectures.lift(indexes.head)
      else {
        @tailrec
        def helper(topic: Option[Topic], indexList: List[Int]): Option[Topic] = {
          topic match {
            case Some(value) => indexList match {
              case ::(head, next) => helper(topic.flatMap(_.subTopics.lift(head)), next)
              case Nil => topic
            }
            case None => None
          }
        }

        helper(lectures.lift(indexes.head).flatMap(_.topics.lift(indexes.tail.head)), indexes.tail.tail.toList)
      }
    } else None

  def update(rawLectures: List[RawLecture]): Lectures = this.copy(rawLectures.map { raw =>
    lectures.find(_.lectureConfig.name == raw.lectureConfig.name)
      .map(_.update(raw))
      .getOrElse(raw.toLecture)
  })

  def updateQuestion(question: Question): Lectures = this.copy(lectures.map { lecture =>
    lecture.copy(topics = lecture.topics.map(topic => topic.copy(questions = topic.questions.map(q => if q.id == question.id then question else q))))
  })

  def filterByQuestion(filterFn: (Question, LectureConfig) => Boolean): Lectures = this.copy(lectures.map(_.filterByQuestion(filterFn)).filter(_.topics.nonEmpty))
}

object Lectures {
  given lecturesEncoder: Encoder[Lectures] = deriveEncoder[Lectures]

  given lecturesDecoder: Decoder[Lectures] = deriveDecoder[Lectures]
}