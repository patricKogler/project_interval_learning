package entities

case class RawLecture(lectureConfig: LectureConfig, rawTopics: List[RawTopic]) {
  def toLecture: Lecture = Lecture(lectureConfig, rawTopics.map(_.toTopic))
}
