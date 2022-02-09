package helpers

import entities.AnswerState.{Again, Easy, Good, Hard}
import entities.{Answered, LectureConfig, Question}
import org.joda.time.{DateTime, Days}

object question {
  def nextLearningDate(question: Question, lectureConfig: LectureConfig): DateTime = {
    if question.history.isEmpty then
      val today = DateTime.now()
      val daysToExam = Days.daysBetween(today, lectureConfig.examDate).getDays
      question.createdAt.plusDays(getDaysFromFirstLearn(daysToExam))
    else {
      if question.history.length == 1 then
        val last = question.history.head
        val daysBetweenRevision = Days.daysBetween(question.createdAt, last.answeredAt).getDays
        dateFromLast(question, last, daysBetweenRevision)
      else
        val lastTwo = question.history.takeRight(2)
        val last = lastTwo.last
        val prev = lastTwo.head
        val daysBetweenRevision = Days.daysBetween(prev.answeredAt, last.answeredAt).getDays
        dateFromLast(question, last, daysBetweenRevision)
    }
  }

  private def dateFromLast(question: Question, last: Answered, daysBetweenRevision: Int) = {
    val ef = getEFactor(question.history)
    if last.answerState == Again then DateTime.now()
    else last.answeredAt.plusDays(Math.ceil((if daysBetweenRevision == 0 then 1 else daysBetweenRevision) * ef))
  }

  private def getEFactor(answers: List[Answered]): Double = {

    /**
     * calculate according to pm2 algorithm
     *
     * @param answerQuality
     * @return Double
     */
    def plusEf(answerQuality: Double): Double = 0.1 - (5 - answerQuality) * (0.08 + (5 - answerQuality) * 0.02)

    answers.foldLeft(2.5) { (ef, answer) =>
      val plEf = answer.answerState match {
        case Again => plusEf(0)
        case Hard => plusEf(1.67)
        case Good => plusEf(3.34)
        case Easy => plusEf(5)
      }
      ef + plEf
    }
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
