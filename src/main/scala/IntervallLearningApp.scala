import file.helpers.parse.{Parse, ParseLive}
//import repos.topics.{TopicsRepo, TopicsRepoLive}
import zio.*
import module.index.{IndexLectures, IndexLecturesLive}
import os.Path
import zio.Console.{printLine, readLine}
import providers.path.PathProviderLive
import repos.lectures.{LecturesRepo, LecturesRepoLive}
import services.question.QuestionServiceLive
import services.topic.review.{UserReviewService, UserReviewServiceLive}
import services.topic.selection.{UserSelection, UserSelectionLive}
import zio.process.Command

import scala.io.AnsiColor.*
import scala.io.Source

object IntervallLearningApp extends zio.ZIOAppDefault {

  override def run = logic.provide(
    PathProviderLive.layer,
    LecturesRepoLive.layer,
    ParseLive.layer,
    Console.live,
    IndexLecturesLive.layer,
    UserReviewServiceLive.layer,
    QuestionServiceLive.layer,
    UserSelectionLive.layer,
  )


  def indexLectures: ZIO[IndexLectures, String, Unit] = for {
    _ <- IndexLectures.indexLectures
  } yield ()

  def logic = for {
    _ <- printLine("Indexing Lectures...")
    _ <- IndexLectures.indexLectures
    _ <- printLine("Weighing Questions...")
    _ <- printLine("\u001b[H")
    lecturesToReview <- LecturesRepo.getAllLectures.map(_.filterByQuestion { (question, config) =>
      helpers.question.nextLearningDate(question, config).isBeforeNow
    })
    _ <- if lecturesToReview.lectures.nonEmpty
    then UserSelection.getSelection(lecturesToReview).map(UserReviewService.review)
    else printLine("Nothing to review")
  } yield ()
}
