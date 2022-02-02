package entities

import scala.annotation.tailrec

case class WeightedTopics(weightedTopics: List[TopicWithWeightedIntervalQuestions]) {
  override def toString: String =
    val stringBuilder: StringBuilder = new StringBuilder()

    @tailrec
    def helper(weightedTopics: List[TopicWithWeightedIntervalQuestions], stringBuilder: StringBuilder, current: Int): StringBuilder =
      weightedTopics match {
        case Nil => stringBuilder
        case (topic: TopicWithWeightedIntervalQuestions) :: tail: List[TopicWithWeightedIntervalQuestions] =>
          stringBuilder.append(s"${current}) ${topic.name} (${topic.questions.count(_.shouldReviewNow)}) \n")
          helper(tail, stringBuilder, current + 1)
      }

    helper(weightedTopics, stringBuilder, 0).toString()

}
