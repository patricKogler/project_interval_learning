package repos

import entities.*
import zio.json.*
import providers.path.PathProvider
import zio.*

object lectures {
  trait LecturesRepo {
    def saveLectures(lectures: Lectures): IO[String, Unit]

    def getAllLectures: IO[String, Lectures]

    def getAllLecturesToReview: IO[String, Lectures]
  }

  case class LecturesRepoLive(pathProvider: PathProvider)(using encoder: JsonEncoder[Lectures]) extends LecturesRepo {
    override def saveLectures(lectures: Lectures): IO[String, Unit] = for {
      file <- pathProvider.getLecturesFile
      _ <- Task.succeed(os.write.over(file, lectures.toJson))
    } yield ()

    override def getAllLectures: IO[String, Lectures] = for {
      file <- pathProvider.getLecturesFile
      lectures <- Task.succeed(os.read(file).fromJson[Lectures].getOrElse(Lectures()))
    } yield lectures

    override def getAllLecturesToReview: IO[String, Lectures] = for {
      ls <- getAllLectures
      lecturesToReview = ls.lectures.map(_.filterByQuestion { (question, config) =>
        helpers.question.nextLearningDate(question, config).isBeforeNow
      })
    } yield Lectures(lecturesToReview)
  }

  object LecturesRepoLive {
    def layer(using encoder: JsonEncoder[Lectures]): ZLayer[PathProvider, String, LecturesRepo] = ZLayer.fromService(LecturesRepoLive(_))
  }

  object LecturesRepo {
    def saveLectures(lectures: Lectures): ZIO[LecturesRepo, String, Unit] = ZIO.serviceWithZIO[LecturesRepo](_.saveLectures(lectures))

    def getAllLectures: ZIO[LecturesRepo, String, Lectures] = ZIO.serviceWithZIO[LecturesRepo](_.getAllLectures)

    def getAllLecturesToReview: ZIO[LecturesRepo, String, Lectures] = ZIO.serviceWithZIO[LecturesRepo](_.getAllLecturesToReview)
  }
}
