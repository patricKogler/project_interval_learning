package entities

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.*
import io.circe.syntax.*
import org.joda.time.DateTime

import java.util.UUID


case class Question(question: String, history: List[Answered] = List.empty, id: UUID = UUID.randomUUID(), createdAt: DateTime = DateTime.now())

object Question {
  given questionEncoder: Encoder[Question] = deriveEncoder

  given questionDecoder: Decoder[Question] = deriveDecoder
}
