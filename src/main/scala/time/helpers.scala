package time

import com.github.nscala_time.time
import com.github.nscala_time.time.Imports
import com.github.nscala_time.time.Imports.*
import io.circe.{Decoder, Encoder}

import scala.util.Try


object helpers {
  given dateTimeEncoder: Encoder[DateTime] = Encoder.encodeString.contramap(_.toString)

  given dateTimeDecoder: Decoder[DateTime] = Decoder.decodeString.emapTry(str => Try(new DateTime(str)))
}
