package entities

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._
import io.circe.syntax.*

import com.github.nscala_time.time.Imports._
import time.helpers.{dateTimeEncoder, dateTimeDecoder}

case class Answered(answeredAt: DateTime, result: AnswerResult)

object Answered {
  given answeredEncoder: Encoder[Answered] = deriveEncoder[Answered]

  given answeredDecoder: Decoder[Answered] = deriveDecoder[Answered]
}
