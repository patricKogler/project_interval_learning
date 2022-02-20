//import entities.{Lectures, Question, Topic, Topics}
//import zio.json.*
//import providers.path.PathProvider
//import zio.{Has, Task, ZIO, ZLayer}
//
//package object repos {
//
//  object topics {
//    sealed trait TopicsRepo {
//      def saveTopics(toSave: Topics): Task[Unit]
//
//      def getTopics: Task[Topics]
//
//      def updateTopic(toUpdate: Topic): Task[Unit]
//
//      def updateQuestion(question: Question, topicName: String): Task[Unit]
//    }
//
//    case class TopicsRepoLive(pathProvider: PathProvider)(using JsonEncoder[Topics])(using JsonDecoder[Topics]) extends TopicsRepo {
//      override def saveTopics(toSave: Topics): Task[Unit] = pathProvider.getLecturesFile.map { saveTo =>
//        os.write.over(saveTo, JsonEncoder.toJson[Topics](toSave))
//      }
//
//      override def getTopics: Task[Topics] = pathProvider.getLecturesFile.map { saveTo =>
//        if os.exists(saveTo) then os.read(saveTo).fromJson[Topics].getOrElse(Topics(List.empty))
//        else Topics(List.empty)
//      }
//
//      override def updateTopic(toUpdate: Topic): Task[Unit] = for {
//        oldTopics <- getTopics
//        updated <- Task.attempt(oldTopics.copy(topics = oldTopics.topics.map(t => if t.name == toUpdate.name then toUpdate else t)))
//        _ <- saveTopics(updated)
//      } yield ()
//
//      override def updateQuestion(question: Question, topicName: String): Task[Unit] = for {
//        oldTopics <- getTopics
//        updated <- Task.attempt(oldTopics.copy(topics = oldTopics.topics.map { topic =>
//          if topic.name == topicName then
//            topic.copy(questions = topic.questions.map(q => if q.question == question.question then question else q))
//          else topic
//        }))
//        _ <- saveTopics(updated)
//      } yield ()
//    }
//
//    object TopicsRepoLive {
//      def layer(using encoder: JsonEncoder[Topics]): ZLayer[PathProvider, Any, TopicsRepo] = ZLayer.fromService(TopicsRepoLive(_))
//    }
//
//    object TopicsRepo {
//      def saveTopics(toSave: Topics): ZIO[TopicsRepo, Throwable, Unit] = ZIO.serviceWithZIO[TopicsRepo](_.saveTopics(toSave))
//
//      def getTopics: ZIO[TopicsRepo, Throwable, Topics] = ZIO.serviceWithZIO[TopicsRepo](_.getTopics)
//    }
//  }
//
//
//}
