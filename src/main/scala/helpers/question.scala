package helpers

import entities.*
import entities.AnswerState.*
import org.joda.time.{DateTime, Days}

import scala.annotation.tailrec

object question {

  def getQuestionWeight(question: Question, lectureConfig: LectureConfig): Double =
    val nextReview = nextLearningDate(question, lectureConfig)
    if nextReview.isBefore(DateTime.now()) then {
      if question.history.isEmpty then {
        4 + (0.2 * Days.daysBetween(nextReview, DateTime.now()).getDays)
      } else {
        answerStateWeight(question.history.last.answerState) + (0.2 * Days.daysBetween(nextReview, DateTime.now()).getDays)
      }
    } else 0

  private def answerStateWeight(answerState: AnswerState): Double = answerState match {
    case Again => 4.0
    case Hard => 3.0
    case Good => 2
    case Easy => 1
  }

  def nextLearningDate(question: Question, lectureConfig: LectureConfig): DateTime = {
    getEFactorWithDate(question, lectureConfig)._2
  }

  private def getEFactorWithDate(question: Question, config: LectureConfig): (Double, DateTime) = {
    val answers = question.history

    /**
     * calculate according to pm2 algorithm
     *
     * @param answerQuality
     * @return Double
     */
    def plusEf(answerQuality: Double): Double = 0.1 - (5 - answerQuality) * (0.08 + (5 - answerQuality) * 0.02)

    def getEfFromAnswer(oldEf: Double, answered: Answered, shouldHaveReviewedOn: DateTime) = {
      val plEf = answered.answerState match {
        case Again => plusEf(0)
        case Hard => plusEf(1.67)
        case Good => plusEf(3.34)
        case Easy => plusEf(5)
      }

      val ef = oldEf + plEf + (if shouldHaveReviewedOn.isBefore(answered.answeredAt)
      then Days.daysBetween(answered.answeredAt, shouldHaveReviewedOn).getDays * 0.05 else Days.daysBetween(answered.answeredAt, shouldHaveReviewedOn).getDays * 0.02)
      // according to pm 2 otherwise question comes to often
      if ef < 1.3 then 1.3 else ef
    }

    @tailrec
    def helper(answers: List[Answered], ef: Double, shouldHaveReviewedOn: DateTime, daysFromLast: Int): (Double, DateTime) = {
      answers match {
        case head :: next =>
          val nextDaysFromLast = Math.min(90, Math.ceil((if daysFromLast == 0 then 1 else daysFromLast) * ef).toInt)
          val nextEf = getEfFromAnswer(ef, head, shouldHaveReviewedOn)
          helper(next, nextEf, shouldHaveReviewedOn.plusDays(nextDaysFromLast), nextDaysFromLast)
        case Nil => (ef, shouldHaveReviewedOn)
      }
    }

    val today = DateTime.now()
    val daysToExam = Days.daysBetween(today, config.examDate).getDays
    val firstReview = question.createdAt.plusDays(getDaysFromFirstLearn(daysToExam))
    helper(question.history, 2.5, firstReview, Days.daysBetween(question.createdAt, firstReview).getDays)
  }

  private def getDaysFromFirstLearn(daysToExam: Int): Int = {
    if daysToExam < 7 then return 0
    if daysToExam < 15 then return 2
    if daysToExam < 30 then return 5
    if daysToExam < 60 then return 8
    if daysToExam < 90 then return 14
    if daysToExam < 180 then 21
    else 30
  }
}
