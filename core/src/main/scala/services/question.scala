package services

import entities.Question
import repos.lectures.LecturesRepo
import zio.*

object question {
  trait QuestionService {
    def updateQuestion(question: Question): IO[String, Unit]
  }

  case class QuestionServiceLive(lecturesRepo: LecturesRepo) extends QuestionService {
    override def updateQuestion(question: Question): IO[String, Unit] = for {
      lectures <- lecturesRepo.getAllLectures
      _ <- lecturesRepo.saveLectures(lectures.updateQuestion(question))
    } yield ()
  }

  object QuestionServiceLive {
    def layer: ZLayer[LecturesRepo, String, QuestionService] = (QuestionServiceLive(_)).toLayer[QuestionService]
  }
}
