package entities

case class Lecture(lectureConfig: LectureConfig, topics: List[Topic]) {
  def update(rawLecture: RawLecture): Lecture = Lecture(rawLecture.lectureConfig, rawLecture.rawTopics.map { rawTopic =>
    topics.find(_.name == rawTopic.name)
      .map(topic => topic.copy(questions = rawTopic.questions.map(rq => topic.questions.find(_.question == rq).getOrElse(Question(rq)))))
      .getOrElse(rawTopic.toTopic)
  })
}
