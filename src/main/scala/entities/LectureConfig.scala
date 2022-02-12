package entities

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import org.joda.time.DateTime
import time.helpers.{dateTimeEncoder, dateTimeDecoder}

case class LectureConfig(name: String, examDate: DateTime)

object LectureConfig {
  given lectureConfigEncoder: Encoder[LectureConfig] = deriveEncoder[LectureConfig]

  given lectureConfigDecoder: Decoder[LectureConfig] = deriveDecoder[LectureConfig]
}
