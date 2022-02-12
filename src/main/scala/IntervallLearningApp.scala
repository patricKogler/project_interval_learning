import file.helpers.parse.{Parse, ParseLive}
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
import providers.path.PathProviderLive
import repos.lectures.{LecturesRepo, LecturesRepoLive}
import services.question.QuestionServiceLive
import services.topic.review.{UserReviewService, UserReviewServiceLive}
import services.topic.selection.{UserSelection, UserSelectionLive}
import zio.process.Command

import scala.io.AnsiColor.*
import scala.io.Source

object IntervallLearningApp extends zio.App {

  import entities.Lectures.{lecturesEncoder, lecturesDecoder}

  private val topicsRepoLayer = PathProviderLive.layer >>> TopicsRepoLive.layer
  private val lecturesRepoLayer = PathProviderLive.layer >>> LecturesRepoLive.layer
  private val indexQuestionsLayer =
    (lecturesRepoLayer
      ++ (PathProviderLive.layer >>> ParseLive.layer)) >>>
      IndexLecturesLive.layer
  private val userSelectionLayer = Console.live >>> UserSelectionLive.layer
  private val questionServiceLayer = lecturesRepoLayer >>> QuestionServiceLive.layer
  private val userReviewServiceLayer = (Console.live ++ questionServiceLayer) >>> UserReviewServiceLive.layer


  def logicLayer =
    ZLayer.identity[Console] ++ indexQuestionsLayer ++ zio.blocking.Blocking.live ++ lecturesRepoLayer ++ userSelectionLayer ++ userReviewServiceLayer

  override def run(args: List[String]) =
    logic.provideLayer(logicLayer).exitCode

  def logic: ZIO[zio.blocking.Blocking with Console with Has[IndexLectures] with Has[LecturesRepo] with Has[UserSelection] with Has[UserReviewService], Throwable, Unit] = for {
    _ <- putStrLn("Indexing Lectures...")
    _ <- IndexLectures.indexLectures
    _ <- putStrLn("Weighing Questions...")
    _ <- putStrLn("\u001b[H")
    lecturesToReview <- LecturesRepo.getAllLectures.map(_.filterByQuestion { (question, config) =>
      helpers.question.nextLearningDate(question, config).isBeforeNow
    })
    _ <- if lecturesToReview.lectures.nonEmpty
    then UserSelection.getSelection(lecturesToReview).flatMap(UserReviewService.review)
    else putStrLn("Nothing to review")
  } yield ()
}
