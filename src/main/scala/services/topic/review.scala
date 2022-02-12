package services.topic

import entities.{AnswerState, Lectures, Question, Topic}
import services.question.QuestionService
import zio.*
import zio.console.Console
import helpers.question.*

import scala.annotation.tailrec

object review {
  trait UserReviewService {
    def review(lectures: Lectures): Task[Unit]
  }

  case class UserReviewServiceLive(console: Console.Service, questionService: QuestionService) extends UserReviewService {

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

    def questionAsker(lectures: Lectures): Task[Unit] = {
      val questions = getQuestionsByWeight(lectures)

      def clear = console.putStrLn("\u001b[2J")

      def helper(questions: List[Question]): Task[Unit] = {
        questions match {
          case head :: next => for {
            _ <- console.putStrLn(getQuestionStringWithPath(lectures, head))
            _ <- console.putStrLn("Again    Hard    Good    Easy")
            _ <- console.putStrLn("  1        2       3       4 ")
            answer <- console.getStrLn
            _ <- saveAnswer(answer, head)
            _ <- clear
            _ <- console.putStrLn("Next Question? [y]/n").when(next.nonEmpty)
            nextQuestion <- if next.nonEmpty then console.getStrLn.map(_ != "n") else Task.succeed(false)
            _ <- clear
            _ <- helper(next).when(nextQuestion)
          } yield ()
          case Nil => Task.unit
        }
      }

      helper(questions)
    }

    override def review(lectures: Lectures): Task[Unit] = questionAsker(lectures)
  }

  object UserReviewServiceLive {
    def layer: ZLayer[Console with Has[QuestionService], Throwable, Has[UserReviewService]] = (UserReviewServiceLive(_, _)).toLayer
  }

  object UserReviewService {
    def review(lectures: Lectures): ZIO[Has[UserReviewService], Throwable, Unit] = ZIO.serviceWith[UserReviewService](_.review(lectures))
  }
}
