import .parse.{Parse, ParseLive}
import repos.topics.{TopicsRepo, TopicsRepoLive}
import io.circe.Encoder
import zio.{ExitCode, Has, IO, RIO, Schedule, Task, ULayer, URIO, ZEnv, ZIO, ZLayer}
import module.index.{IndexLectures, IndexLecturesLive}
import os.Path
import zio.console.{Console, getStrLn, putStrLn}
import io.circe.generic.semiauto.*
import io.circe.syntax.*
import io.circe.parser.decode
import entities.{Question, Topic}
import providers.interval.{IntervalProvider, IntervalProviderLive}
import providers.path.PathProviderLive
import repos.lectures.LecturesRepoLive
import services.review.{UserReviewService, UserReviewServiceLive}
import services.selection.{UserSelectionService, UserSelectionServiceLive}
import services.topics.{WeightedTopicsService, WeightedTopicsServiceLive}
import zio.process.Command

import scala.io.AnsiColor.*
import scala.io.Source

object IntervallLearningApp extends zio.App {

  import entities.Lectures.{lecturesEncoder, lecturesDecoder}

  private val topicsRepoLayer = PathProviderLive.layer >>> TopicsRepoLive.layer
  private val lecturesRepoLayer = PathProviderLive.layer >>> LecturesRepoLive.layer
  val indexQuestionsLayer =
    (lecturesRepoLayer
      ++ (PathProviderLive.layer >>> ParseLive.layer)) >>>
      IndexLecturesLive.layer

  val weightedTopicsLayer = (topicsRepoLayer ++ IntervalProviderLive.layer) >>> WeightedTopicsServiceLive.layer

  val userReviewServiceLayer = (Console.live ++ topicsRepoLayer) >>> UserReviewServiceLive.layer

  def logicLayer =
    ZLayer.identity[Console] ++ indexQuestionsLayer ++ weightedTopicsLayer ++ UserSelectionServiceLive.layer ++ userReviewServiceLayer ++ zio.blocking.Blocking.live

  override def run(args: List[String]) =
    logic.provideLayer(logicLayer).exitCode

  def logic: ZIO[zio.blocking.Blocking with Console with Has[UserReviewService] with Has[UserSelectionService] with Has[WeightedTopicsService] with Has[IndexLectures], Throwable, Unit] = for {
    _ <- putStrLn("Indexing Lectures...")
    _ <- IndexLectures.indexLectures
    _ <- putStrLn("Weighing Questions...")
    _ <- putStrLn("\u001b[H")
    weightedTopics <- WeightedTopicsService.getWeightedTopics
    topicsToReview <- UserSelectionService.getUserSelection(weightedTopics.copy(weightedTopics.weightedTopics.filter(_.questions.exists(_.shouldReviewNow))))
    _: ExitCode <- UserReviewService.reviewQuestions(topicsToReview).exitCode
  } yield ()

}
