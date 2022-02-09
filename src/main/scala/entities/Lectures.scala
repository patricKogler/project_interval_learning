package entities

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import Lecture.{lectureDecoder, lectureEncoder}

import scala.util.Try

case class Lectures(lectures: List[Lecture] = List.empty) {
  def update(rawLectures: List[RawLecture]): Lectures = this.copy(rawLectures.map { raw =>
    lectures.find(_.lectureConfig.name == raw.lectureConfig.name)
      .map(_.update(raw))
      .getOrElse(raw.toLecture)
  })

  def updateQuestion(question: Question): Lectures = this.copy(lectures.map { lecture =>
    lecture.copy(topics = lecture.topics.map(topic => topic.copy(questions = topic.questions.map(q => if q.id == question.id then question else q))))
  })
}

object Lectures {
  given lecturesEncoder: Encoder[Lectures] = deriveEncoder[Lectures]

  given lecturesDecoder: Decoder[Lectures] = deriveDecoder[Lectures]
}