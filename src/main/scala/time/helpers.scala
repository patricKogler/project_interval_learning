package time

import com.github.nscala_time.time
import com.github.nscala_time.time.Imports
import com.github.nscala_time.time.Imports.*
import io.circe.{Decoder, Encoder}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatter

import scala.util.Try


object helpers {
  val formatString = "d.M.Y H:m"

  given dateTimeEncoder: Encoder[DateTime] = {
    Encoder.encodeString.contramap(_.toString(formatString))
  }

  given dateTimeDecoder: Decoder[DateTime] = Decoder.decodeString.emapTry(str => Try(DateTimeFormat.forPattern(formatString).parseDateTime(str)))
}
