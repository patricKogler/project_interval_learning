import com.github.nscala_time.time.StaticForwarderImports.Interval
import com.typesafe.config.ConfigFactory
import entities.AnswerResult.Wrong
import entities.Difficulty.{High, Low, Medium}
import entities.{AnswerResult, Answered, Difficulty, Question, WeightedInterval}
import org.joda.time.{DateTime, Days}
import os.Path
import zio.{Has, Task, UIO, ULayer, ZIO, ZLayer}

package object providers {

  object path {
    trait PathProvider {
      def getBasePath: Task[Path]

      def getQuestionsDir: Task[Path]

      def getTopicsFile: Task[Path]
    }

    case class PathProviderLive() extends PathProvider {

      private lazy val baseDir = os.Path(ConfigFactory.load().getString("basedir"));
      private lazy val questionsDir = baseDir / "_questions"
      private lazy val topicsFile = questionsDir / "topics.json"

      override def getBasePath: Task[Path] = Task.effect(baseDir)

      override def getQuestionsDir: Task[Path] = for {
        basePath <- getBasePath
        qdir <- Task.effect {
          if !os.exists(questionsDir) then os.makeDir(questionsDir)
          questionsDir
        }
      } yield qdir

      override def getTopicsFile: Task[Path] = for {
        _ <- getQuestionsDir
        topics <- Task.effect {
          if os.exists(topicsFile) then topicsFile
          else
            os.write(topicsFile, "")
            topicsFile
        }
      } yield topics
    }

    object PathProviderLive {
      def layer: ULayer[Has[PathProvider]] = ZLayer.succeed(PathProviderLive())
    }
  }

  object interval {
    trait IntervalProvider {
      def getWeightedInterval(question: Question): Task[WeightedInterval]
    }

    case class IntervalProviderLive() extends IntervalProvider {

      private def getWeight(answers: List[Answered]): Double = {
        if answers.isEmpty then 1
        else answers.last.result match {
          case AnswerResult.Correct(difficulty) => difficulty match {
            case Difficulty.High => 0.8
            case Difficulty.Medium => 0.5
            case Difficulty.Low => 0.2
          }
          case AnswerResult.Wrong => 1
        }
      }

      private def review(question: Question) = WeightedInterval(question, getWeight(question.history), true)

      private def discard(question: Question) = WeightedInterval(question, 0, false)

      private def shouldReview(answers: List[Answered]): Boolean =
        val answered = answers.length
        val daysSinceFirstAnswer: Int = Days.daysBetween(answers.head.answeredAt, DateTime.now()).getDays

        val lastThree = answers.takeRight(3)
        val logBase = lastThree.foldLeft(0d)((b, answered) => b + (answered.result match {
          case AnswerResult.Correct(difficulty) => difficulty match {
            case Difficulty.High => 1.8d
            case Difficulty.Medium => 1.0d
            case Difficulty.Low => 2.2d
          }
          case AnswerResult.Wrong => 1.7d
        })) / lastThree.length

        answered < (Math.log(daysSinceFirstAnswer) / Math.log(logBase))

      override def getWeightedInterval(question: Question): Task[WeightedInterval] = Task.effect {
        if question.history.isEmpty then review(question)
        else question.history.last.result match {
          case AnswerResult.Correct(difficulty) =>
            if shouldReview(question.history) then review(question)
            else discard(question)
          case AnswerResult.Wrong => review(question)
        }
      }
    }

    object IntervalProviderLive {
      def layer: ULayer[Has[IntervalProvider]] = ZLayer.succeed(IntervalProviderLive())
    }

    object IntervalProvider {
      def getWeightedInterval(question: Question): ZIO[Has[IntervalProvider], Throwable, WeightedInterval] =
        ZIO.serviceWith[IntervalProvider](_.getWeightedInterval(question))
    }
  }

}
