import entities.{Question, Topic, Topics}
import io.circe.jawn.decode
import io.circe.{Decoder, Encoder}
import providers.path.PathProvider
import zio.{Has, Task, ZIO, ZLayer}

package object repos {

  object topics {
    sealed trait TopicsRepo {
      def saveTopics(toSave: Topics): Task[Unit]

      def getTopics: Task[Topics]

      def updateTopic(toUpdate: Topic): Task[Unit]

      def updateQuestion(question: Question, topicName: String): Task[Unit]
    }

    case class TopicsRepoLive(pathProvider: PathProvider)(using encoder: Encoder[Topics])(using decoder: Decoder[Topics]) extends TopicsRepo {
      override def saveTopics(toSave: Topics): Task[Unit] = pathProvider.getTopicsFile.map { saveTo =>
        os.write.over(saveTo, encoder(toSave).toString)
      }

      override def getTopics: Task[Topics] = pathProvider.getTopicsFile.map { saveTo =>
        if os.exists(saveTo) then decode[Topics](os.read(saveTo)).getOrElse(Topics(List.empty))
        else Topics(List.empty)
      }

      override def updateTopic(toUpdate: Topic): Task[Unit] = for {
        oldTopics <- getTopics
        updated <- Task.effect(oldTopics.copy(topics = oldTopics.topics.map(t => if t.name == toUpdate.name then toUpdate else t)))
        _ <- saveTopics(updated)
      } yield ()

      override def updateQuestion(question: Question, topicName: String): Task[Unit] = for {
        oldTopics <- getTopics
        updated <- Task.effect(oldTopics.copy(topics = oldTopics.topics.map { topic =>
          if topic.name == topicName then
            topic.copy(questions = topic.questions.map(q => if q.question == question.question then question else q))
          else topic
        }))
        _ <- saveTopics(updated)
      } yield ()
    }

    object TopicsRepoLive {
      def layer(using encoder: Encoder[Topics]): ZLayer[Has[PathProvider], Any, Has[TopicsRepo]] = ZLayer.fromService(TopicsRepoLive(_))
    }

    object TopicsRepo {
      def saveTopics(toSave: Topics): ZIO[Has[TopicsRepo], Throwable, Unit] = ZIO.serviceWith[TopicsRepo](_.saveTopics(toSave))

      def getTopics: ZIO[Has[TopicsRepo], Throwable, Topics] = ZIO.serviceWith[TopicsRepo](_.getTopics)
    }
  }

}
