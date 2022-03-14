package time

import com.github.nscala_time.time
import com.github.nscala_time.time.Imports
import com.github.nscala_time.time.Imports.*
import zio.json.*
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatter
import scala.util.Try

object helpers {
  val formatString = "d.M.Y H:m"

  given dateTimeEncoder: JsonEncoder[DateTime] = JsonEncoder[String].contramap(_.toString(formatString))

  given dateTimeDecoder: JsonDecoder[DateTime] = JsonDecoder[String].map[DateTime](str => DateTimeFormat.forPattern(formatString).parseDateTime(str))
}
