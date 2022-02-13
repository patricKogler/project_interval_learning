package entities

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.*
import io.circe.syntax.*
import time.helpers.{dateTimeDecoder, dateTimeEncoder}

import java.util.UUID

case class Topic(name: String, questions: List[Question] = List.empty, id: UUID = UUID.randomUUID(), subTopics: List[Topic] = List.empty) {
  def filterQuestions(filterFn: (Question, LectureConfig) => Boolean, lectureConfig: LectureConfig): Topic = {
    this.copy(
      questions = questions.filter(q => filterFn(q, lectureConfig)),
      subTopics = subTopics.map(_.filterQuestions(filterFn, lectureConfig)).filter(_.questions.nonEmpty)
    )
  }
    
  def getAllQuestions: List[Question] = questions.appendedAll(subTopics.flatMap(_.getAllQuestions))
}

object Topic {
  given topicEncoder: Encoder[Topic] = deriveEncoder[Topic]

  given topicDecoder: Decoder[Topic] = deriveDecoder[Topic]
}
