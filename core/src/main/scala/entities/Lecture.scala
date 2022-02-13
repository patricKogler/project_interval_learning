package entities

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.joda.time.DateTime
import time.helpers.{dateTimeEncoder, dateTimeDecoder}

import java.util.UUID

case class Lecture(lectureConfig: LectureConfig, topics: List[Topic] = List.empty, createdAt: DateTime = DateTime.now(), id: UUID = UUID.randomUUID()) {
  def update(rawLecture: RawLecture): Lecture = this.copy(rawLecture.lectureConfig, rawLecture.rawTopics.map { rawTopic =>
    topics.find(_.name == rawTopic.name)
      .map(topic => topic.copy(questions = rawTopic.questions.map(rq => topic.questions.find(_.question == rq).getOrElse(Question(rq)))))
      .getOrElse(rawTopic.toTopic)
  })
  
  def getAllQuestions: List[Question] = topics.flatMap(_.getAllQuestions)

  def filterByQuestion(filterFn: (Question, LectureConfig) => Boolean): Lecture = this.copy(topics = topics.map(_.filterQuestions(filterFn, lectureConfig)).filter(_.questions.nonEmpty))
}

object Lecture {
  given lectureEncoder: Encoder[Lecture] = deriveEncoder[Lecture]

  given lectureDecoder: Decoder[Lecture] = deriveDecoder[Lecture]
}