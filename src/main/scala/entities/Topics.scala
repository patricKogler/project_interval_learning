package entities

import zio.json.*

case class Topics(topics: List[Topic]) {
  def update(raw: List[RawTopic]): Topics = {
    this.copy(raw.map(rawTopic => {
      topics.find(_.name == rawTopic.name)
        .map(topic => topic.copy(questions = raw.find(_.name == topic.name).map(_.questions.map(q => topic.questions.find(_.question == q).getOrElse(Question(q)))).getOrElse(List.empty)))
        .getOrElse(rawTopic.toTopic)
    }))
  }
}

object Topics {
  given topicEncoder: JsonEncoder[Topics] = DeriveJsonEncoder.gen[Topics]

  given topicDecoder: JsonDecoder[Topics] = DeriveJsonDecoder.gen[Topics]
}
