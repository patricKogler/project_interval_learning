package services.topic

import entities.*
import services.question.QuestionService
import zio.*
import zio.Console
import helpers.question.{nextLearningDate, getQuestionWeight}

import scala.annotation.tailrec

object review {
  trait UserReviewService {
    def review(lectures: Lectures): IO[String, Unit]
  }

  case class UserReviewServiceLive(console: Console, questionService: QuestionService) extends UserReviewService {

    private def getQuestionsByWeight(lectures: Lectures): List[Question] = lectures.lectures.flatMap(l => l.getAllQuestions.map(q => (q, l.lectureConfig)))
      .sortBy((q, c) => getQuestionWeight(q, c)).reverse.map(_._1)

    private def getQuestionStringWithPath(lectures: Lectures, question: Question): String = {

      val lectureWithQuestion = lectures.filterByQuestion { (q, lc) =>
        q == question
      }

      @tailrec
      def helper(lts: List[Topic], stringBuilder: StringBuilder): String = {
        if lts.head.questions.contains(question) then stringBuilder.append(s" -> ${lts.head.name}\n${question.question}").toString()
        else helper(lts.head.subTopics, stringBuilder.append(s" -> ${lts.head.name}"))
      }

      helper(lectureWithQuestion.lectures.head.topics, (new StringBuilder).append(lectureWithQuestion.lectures.head.lectureConfig.name))
    }

    private def saveAnswer(answer: String, question: Question) = {
      val updatedQ = answer match {
        case "1" => question.addAnswerFromState(AnswerState.Again)
        case "2" => question.addAnswerFromState(AnswerState.Hard)
        case "3" => question.addAnswerFromState(AnswerState.Good)
        case "4" => question.addAnswerFromState(AnswerState.Easy)
      }
      questionService.updateQuestion(updatedQ)
    }

    def questionAsker(lectures: Lectures): IO[String, Unit] = {
      val questions = getQuestionsByWeight(lectures)

      def clear = console.printLine("\u001b[2J").mapError(_.toString)

      def helper(questions: List[Question]): IO[String, Unit] = {
        questions match {
          case head :: next => for {
            _ <- console.printLine(getQuestionStringWithPath(lectures, head)).mapError(_.toString)
            _ <- console.printLine("Again    Hard    Good    Easy").mapError(_.toString)
            _ <- console.printLine("  1        2       3       4 ").mapError(_.toString)
            answer <- console.readLine.mapError(_.toString)
            _ <- saveAnswer(answer, head)
            _ <- clear
            _ <- console.printLine("Next Question? [y]/n").when(next.nonEmpty).mapError(_.toString)
            nextQuestion <- if next.nonEmpty then console.readLine.mapBoth(_.toString, _ != "n") else IO.succeed(false)
            _ <- clear
            _ <- helper(next).when(nextQuestion)
          } yield ()
          case Nil => IO.unit
        }
      }

      helper(questions)
    }

    override def review(lectures: Lectures): IO[String, Unit] = questionAsker(lectures)
  }

  object UserReviewServiceLive {
    def layer: ZLayer[Console with QuestionService, Throwable, UserReviewService] = (UserReviewServiceLive(_, _)).toLayer
  }

  object UserReviewService {
    def review(lectures: Lectures): ZIO[UserReviewService, String, Unit] = ZIO.serviceWithZIO[UserReviewService](_.review(lectures))
  }
}
