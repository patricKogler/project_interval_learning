package mappers

import entities.{Question, Topic, TopicWithWeightedIntervalQuestions}

object TopicMapper {
  def fromWeightedTopic(topicWithWeightedIntervalQuestions: TopicWithWeightedIntervalQuestions): Topic = {
    Topic(topicWithWeightedIntervalQuestions.name, topicWithWeightedIntervalQuestions.questions.map(_.question))
  }
}
