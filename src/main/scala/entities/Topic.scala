package entities

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._
import io.circe.syntax.*
import time.helpers.{dateTimeDecoder, dateTimeEncoder}

case class Topic(name: String, questions: List[Question] = List.empty)

object Topic {
  given topicEncoder: Encoder[Topic] = deriveEncoder[Topic]

  given topicDecoder: Decoder[Topic] = deriveDecoder[Topic]
}
