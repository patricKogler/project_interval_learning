package entities

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.*
import io.circe.syntax.*
import org.joda.time.DateTime
import time.helpers.{dateTimeEncoder, dateTimeDecoder}
import java.util.UUID


case class Question(question: String, history: List[Answered] = List.empty, id: UUID = UUID.randomUUID(), createdAt: DateTime = DateTime.now()) {
  def addAnswerFromState(answerState: AnswerState): Question = this.copy(history = history.appended(Answered(DateTime.now(), answerState)))
}

object Question {
  given questionEncoder: Encoder[Question] = deriveEncoder[Question]

  given questionDecoder: Decoder[Question] = deriveDecoder[Question]
}
