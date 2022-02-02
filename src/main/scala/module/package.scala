import file.helpers.parse.Parse
import repos.topics.TopicsRepo
import zio.{Has, Task, ZLayer}
import zio._

package object module {

  object index {
    trait IndexQuestions {
      def indexQuestions: Task[Unit]
    }

    case class IndexQuestionsLive(parser: Parse, topicsRepo: TopicsRepo) extends IndexQuestions {
      override def indexQuestions: Task[Unit] = for {
        rawTopics <- parser.parseQuestions
        oldTopics <- topicsRepo.getTopics
        _ <- topicsRepo.saveTopics(oldTopics.update(rawTopics))
      } yield ()
    }

    object IndexQuestionsLive {
      def layer: URLayer[Has[Parse] with Has[TopicsRepo], Has[IndexQuestions]] = (IndexQuestionsLive(_, _)).toLayer
    }

    object IndexQuestions {
      def indexQuestions: RIO[Has[IndexQuestions], Unit] = ZIO.serviceWith[IndexQuestions](_.indexQuestions)
    }
  }

}
