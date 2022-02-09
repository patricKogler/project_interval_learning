package entities

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.scalatest.flatspec._
import org.scalatest.matchers._

class LectureSpec extends AnyFlatSpec with should.Matchers {

  val dateTime: DateTime = DateTime.parse("22.02.2022 15:30", DateTimeFormat.forPattern("d.M.Y H:m"))
  val testLectureConfig: LectureConfig = LectureConfig("Test", AnswerState.Good, dateTime)

  "A lecture" should "not change if RawLecture is not different" in {

    val rawLecture = RawLecture(testLectureConfig, List(RawTopic("Test", List("q1", "q2", "q3")), RawTopic("Test2", List("q1", "q2", "q3"))))
    val lecture = Lecture(testLectureConfig,
      List(
        Topic("Test", List(
          Question("q1", List(Answered(dateTime.minusDays(50), AnswerState.Good))),
          Question("q2"),
          Question("q3")
        )),
        Topic("Test2", List(Question("q1"), Question("q2"), Question("q3")))
      ))

    lecture shouldBe lecture.update(rawLecture)
  }

  it should "remove Question if not in Raw" in {
    val rawLecture = RawLecture(testLectureConfig, List(RawTopic("Test", List("q1", "q2", "q3")), RawTopic("Test2", List("q1", "q3"))))
    val lecture = Lecture(testLectureConfig,
      List(
        Topic("Test", List(
          Question("q1", List(Answered(dateTime.minusDays(50), AnswerState.Good))),
          Question("q2"),
          Question("q3")
        )),
        Topic("Test2", List(Question("q1"), Question("q2"), Question("q3")))
      ))

    val updateLecture = Lecture(testLectureConfig,
      List(
        Topic("Test", List(
          Question("q1", List(Answered(dateTime.minusDays(50), AnswerState.Good))),
          Question("q2"),
          Question("q3")
        )),
        Topic("Test2", List(Question("q1"), Question("q3")))
      ))
    lecture shouldNot equal(lecture.update(rawLecture))
    lecture.update(rawLecture) should equal(updateLecture)

  }
}



