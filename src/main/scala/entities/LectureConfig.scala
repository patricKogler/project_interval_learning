package entities

import zio.json.*
import org.joda.time.DateTime
import time.helpers.{dateTimeEncoder, dateTimeDecoder}
case class LectureConfig(name: String, examDate: DateTime)

object LectureConfig {
  given lectureConfigEncoder: JsonEncoder[LectureConfig] = DeriveJsonEncoder.gen[LectureConfig]

  given lectureConfigDecoder: JsonDecoder[LectureConfig] = DeriveJsonDecoder.gen[LectureConfig]
}
