package entities

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._
import io.circe.syntax.*


case class Question(question: String, history: List[Answered] = List.empty)

object Question {
  given questionEncoder: Encoder[Question] = deriveEncoder

  given questionDecoder: Decoder[Question] = deriveDecoder
}
