package entities

enum AnswerResult:
  case Correct(difficulty: AnswerState)
  case Wrong
