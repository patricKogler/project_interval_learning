import entities.{AnswerResult, Answered, AnswerState, Question, Topic, TopicWithWeightedIntervalQuestions, Topics, WeightedInterval, WeightedTopics}
import mappers.TopicMapper
import org.joda.time.DateTime
import providers.interval.IntervalProvider
import repos.topics.TopicsRepo
import services.topics.WeightedTopicsService
import zio.*
import zio.console.{Console, putStrLn}
import zio.process.Command
import zio.stream.ZStream

import java.io.IOException
import scala.io.StdIn.readLine
import scala.util.matching.Regex

package object services {

  object selection {
    trait UserSelectionService {
      def getUserSelection(weightedTopics: WeightedTopics): Task[Topics]
    }

    case class UserSelectionServiceLive() extends UserSelectionService {

      val IntRegEx: Regex = "(\\d+)".r

      private def fromInput(input: String, weightedTopics: WeightedTopics): List[TopicWithWeightedIntervalQuestions] = {
        (input.toLowerCase() match {
          case "a" => weightedTopics.weightedTopics
          case IntRegEx(num) => List(weightedTopics.weightedTopics(num.toInt))
          case value => value.split(",").filter(IntRegEx.matches).map(_.toInt).map(weightedTopics.weightedTopics(_)).toList
        }).map(t => t.copy(questions = t.questions.sortBy(_.weight).reverse)).sortBy(t => t.questions.foldLeft(0d)(_ + _.weight) / t.questions.length)
      }

      override def getUserSelection(weightedTopics: WeightedTopics): Task[Topics] = for {
        _ <- Task.effect(println("Please Select a Topic (1 or 1,2,4... or a for all)"))
        _ <- Task.effect(println(weightedTopics))
        selection <- Task.effect(fromInput(readLine(), weightedTopics))
        topics <- Task.effect(selection.map(TopicMapper.fromWeightedTopic))
      } yield Topics(topics)
    }

    object UserSelectionServiceLive {
      def layer: ULayer[Has[UserSelectionService]] = ZLayer.succeed(UserSelectionServiceLive())
    }

    object UserSelectionService {
      def getUserSelection(weightedTopics: WeightedTopics): RIO[Has[UserSelectionService], Topics] = ZIO.serviceWith[UserSelectionService](_.getUserSelection(weightedTopics))
    }
  }

//  object review {
//    trait UserReviewService {
//      def reviewQuestions(topics: Topics): Task[Unit]
//    }
//
//    case class UserReviewServiceLive(topicsRepo: TopicsRepo, console: Console.Service) extends UserReviewService {
//
//      import scala.sys.process._
//
//      private def qHelper(questions: List[Question], topic: Topic): Task[Boolean] = questions match {
//        case ::(question, next) => for {
//          _ <- console.putStrLn("c for correct and w for wrong")
//          _ <- console.putStrLn(question.question)
//          correct <- console.getStrLn.map(_.toLowerCase() == "c")
//          difficult <- if correct then for {
//            _ <- console.putStrLn("How difficult was the question from 1 - 3")
//            dif <- console.getStrLn
//          } yield dif else console.putStrLn("")
//          _ <- topicsRepo.updateQuestion(
//            if correct then
//              getDifficulty(question, difficult)
//            else question.copy(history = question.history.appended(Answered(DateTime.now(), AnswerResult.Wrong))), topic.name)
//          _ <- console.putStrLn("Next question [Y] / n")
//          stop <- console.getStrLn.map(_.toLowerCase == "n")
//          _ <- qHelper(next, topic).when(!stop)
//        } yield stop
//        case Nil => Task.effect(false)
//      }
//
//      private def helper(topics: List[Topic]): Task[Unit] =
//        topics match {
//          case ::(topic, next) => for {
//            _ <- console.putStrLn(s"Next Topic: ${topic.name}")
//            stop <- qHelper(topic.questions, topic)
//            _ <- helper(next).when(!stop)
//          } yield ()
//          case Nil => Task.unit
//        }
//
//
//      override def reviewQuestions(topics: Topics): Task[Unit] = Task.unit
//
//      private def getDifficulty(question: Question, difficult: Any) = {
//        difficult match {
//          case "1" => question.copy(history = question.history.appended(Answered(DateTime.now(), AnswerResult.Correct(AnswerState.Low))))
//          case "2" => question.copy(history = question.history.appended(Answered(DateTime.now(), AnswerResult.Correct(AnswerState.Medium))))
//          case _ => question.copy(history = question.history.appended(Answered(DateTime.now(), AnswerResult.Correct(AnswerState.High))))
//        }
//      }
//    }
//
//    object UserReviewServiceLive {
//      def layer: ZLayer[Has[TopicsRepo] with Console, Throwable, Has[UserReviewService]] = (UserReviewServiceLive(_, _)).toLayer
//    }
//
//    object UserReviewService {
//      def reviewQuestions(topics: Topics): ZIO[Has[UserReviewService], Throwable, Unit] = ZIO.serviceWith[UserReviewService](_.reviewQuestions(topics))
//    }
//  }


}
