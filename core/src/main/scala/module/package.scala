import entities.Lecture
import file.helpers.parse.Parse
import repos.lectures.LecturesRepo
import repos.topics.TopicsRepo
import zio.{Has, Task, ZLayer}
import zio.*

package object module {

  object index {
    trait IndexLectures {
      def indexLectures: Task[Unit]
    }

    case class IndexLecturesLive(parser: Parse, lecturesRepo: LecturesRepo) extends IndexLectures {
      override def indexLectures: Task[Unit] = for {
        rawLectures <- parser.parseRawLectures
        oldLectures <- lecturesRepo.getAllLectures
        _ <- lecturesRepo.saveLectures(oldLectures.update(rawLectures))
      } yield ()
    }

    object IndexLecturesLive {
      def layer: URLayer[Has[Parse] with Has[LecturesRepo], Has[IndexLectures]] = (IndexLecturesLive(_, _)).toLayer
    }

    object IndexLectures {
      def indexLectures: RIO[Has[IndexLectures], Unit] = ZIO.serviceWith[IndexLectures](_.indexLectures)
    }
  }

}
