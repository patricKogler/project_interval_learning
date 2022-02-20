import entities.Lecture
import file.helpers.parse.Parse
import repos.lectures.LecturesRepo
//import repos.topics.TopicsRepo
import zio.*

package object module {

  object index {
    trait IndexLectures {
      def indexLectures: IO[String, Unit]
    }

    case class IndexLecturesLive(parser: Parse, lecturesRepo: LecturesRepo) extends IndexLectures {
      override def indexLectures: IO[String, Unit] = for {
        rawLectures <- parser.parseRawLectures
        oldLectures <- lecturesRepo.getAllLectures
        _ <- lecturesRepo.saveLectures(oldLectures.update(rawLectures))
      } yield ()
    }

    object IndexLecturesLive {
      def layer: ZLayer[Parse with LecturesRepo, String, IndexLectures] = (IndexLecturesLive(_, _)).toLayer
    }

    object IndexLectures {
      def indexLectures: ZIO[IndexLectures, String, Unit] = ZIO.serviceWithZIO[IndexLectures](_.indexLectures)
    }
  }

}
