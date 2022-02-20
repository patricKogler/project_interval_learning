package entities

import zio.json.*
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
  given topicEncoder: JsonEncoder[Topic] = DeriveJsonEncoder.gen[Topic]

  given topicDecoder: JsonDecoder[Topic] = DeriveJsonDecoder.gen[Topic]
}
