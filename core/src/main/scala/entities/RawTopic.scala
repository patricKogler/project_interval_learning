package entities

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

case class RawTopic(name: String, questions: List[String]) {
  def toTopic: Topic = Topic(name, questions.map(q => Question(q)))
}


object RawTopic {
  given topicEncoder: Encoder[RawTopic] = deriveEncoder[RawTopic]

  given topicDecoder: Decoder[RawTopic] = deriveDecoder[RawTopic]
}