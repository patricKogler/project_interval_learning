package entities
import zio.json.*

enum AnswerState:
  case Again, Hard, Good, Easy

object AnswerState {
  given answerStateDecoder: JsonDecoder[AnswerState] = DeriveJsonDecoder.gen[AnswerState]

  given answerStateEncoder: JsonEncoder[AnswerState] = DeriveJsonEncoder.gen[AnswerState]
}