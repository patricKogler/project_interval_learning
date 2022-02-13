package repos

import entities.Lectures
import io.circe.jawn.decode
import io.circe.{Decoder, Encoder}
import providers.path.PathProvider
import zio.{Has, Task, ZIO, ZLayer}

object lectures {
  trait LecturesRepo {
    def saveLectures(lectures: Lectures): Task[Unit]

    def getAllLectures: Task[Lectures]

    def getAllLecturesToReview: Task[Lectures]
  }

  case class LecturesRepoLive(pathProvider: PathProvider)(using encoder: Encoder[Lectures]) extends LecturesRepo {
    override def saveLectures(lectures: Lectures): Task[Unit] = for {
      file <- pathProvider.getLecturesFile
      _ <- Task.succeed(os.write.over(file, encoder(lectures).toString))
    } yield ()

    override def getAllLectures: Task[Lectures] = for {
      file <- pathProvider.getLecturesFile
      lectures <- Task.succeed(decode[Lectures](os.read(file)).getOrElse(Lectures()))
    } yield lectures

    override def getAllLecturesToReview: Task[Lectures] = for {
      ls <- getAllLectures
      lecturesToReview = ls.lectures.map(_.filterByQuestion { (question, config) =>
        helpers.question.nextLearningDate(question, config).isBeforeNow
      })
    } yield Lectures(lecturesToReview)
  }

  object LecturesRepoLive {
    def layer(using encoder: Encoder[Lectures]): ZLayer[Has[PathProvider], Nothing, Has[LecturesRepo]] = ZLayer.fromService(LecturesRepoLive(_))
  }

  object LecturesRepo {
    def saveLectures(lectures: Lectures): ZIO[Has[LecturesRepo], Throwable, Unit] = ZIO.serviceWith[LecturesRepo](_.saveLectures(lectures))

    def getAllLectures: ZIO[Has[LecturesRepo], Throwable, Lectures] = ZIO.serviceWith[LecturesRepo](_.getAllLectures)

    def getAllLecturesToReview: ZIO[Has[LecturesRepo], Throwable, Lectures] = ZIO.serviceWith[LecturesRepo](_.getAllLecturesToReview)
  }
}
