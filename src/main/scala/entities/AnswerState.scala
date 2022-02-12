package entities

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

enum AnswerState:
  case Again, Hard, Good, Easy

object AnswerState {
  given answerStateDecoder: Decoder[AnswerState] = deriveDecoder[AnswerState]

  given answerStateEncoder: Encoder[AnswerState] = deriveEncoder[AnswerState]
}