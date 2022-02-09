package services

import entities.Question
import repos.lectures.LecturesRepo
import zio.Task

object question {
  trait QuestionService {
    def updateQuestion(question: Question): Task[Unit]
  }

  case class QuestionServiceLive(lecturesRepo: LecturesRepo) extends QuestionService {
    override def updateQuestion(question: Question): Task[Unit] = for {
      lectures <- lecturesRepo.getAllLectures
      _ <- lecturesRepo.saveLectures(lectures.updateQuestion(question))
    } yield ()
  }
}
