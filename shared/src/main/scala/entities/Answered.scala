package entities

import org.joda.time.DateTime
import zio.json.*
import time.helpers.{dateTimeDecoder, dateTimeEncoder}

case class Answered(answeredAt: DateTime, answerState: AnswerState)

object Answered {
  given answeredEncoder: JsonEncoder[Answered] = DeriveJsonEncoder.gen[Answered]

  given answeredDecoder: JsonDecoder[Answered] = DeriveJsonDecoder.gen[Answered]
}
