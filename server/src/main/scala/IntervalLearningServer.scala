import zhttp.http._
import zhttp.service.Server
import zio._
import zio.json.{EncoderOps, JsonEncoder}

import java.net.URLDecoder

object IntervalLearningServer extends ZIOAppDefault {

  def taskHandler[A](task: Task[A])(implicit enc: JsonJsonEncoder[A]): UIO[Response] = {
    task.either.map {
      case Left(value) => value.toString
      case Right(value) => value.toJson
    }.map(Response.json)
  }

  val app: HttpApp[Any, Nothing] = Http.collectZIO[Request] {
    case Method.GET -> !! / "text" / message => UIO(Response.text(message, HTTP_CHARSET))
    case Method.GET -> !! / "json" => UIO(Response.json("""{"greetings": "Hello World!"}"""))
  }

  // Run it like any simple app
  def run: URIO[zio.ZEnv, ExitCode] =
    Server.start(8090, app).exitCode
}
