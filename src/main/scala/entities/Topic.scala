package entities

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.*
import io.circe.syntax.*
import time.helpers.{dateTimeDecoder, dateTimeEncoder}

import java.util.UUID

case class Topic(name: String, questions: List[Question] = List.empty, id: UUID = UUID.randomUUID())

object Topic {
  given topicEncoder: Encoder[Topic] = deriveEncoder[Topic]

  given topicDecoder: Decoder[Topic] = deriveDecoder[Topic]
}
