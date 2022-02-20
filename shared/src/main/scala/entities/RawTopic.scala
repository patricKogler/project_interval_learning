package entities

import zio.json.*

case class RawTopic(name: String, questions: List[String]) {
  def toTopic: Topic = Topic(name, questions.map(q => Question(q)))
}


object RawTopic {
  given topicEncoder: JsonEncoder[RawTopic] = DeriveJsonEncoder.gen[RawTopic]

  given topicDecoder: JsonDecoder[RawTopic] = DeriveJsonDecoder.gen[RawTopic]
}