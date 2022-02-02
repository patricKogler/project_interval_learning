package entities

enum AnswerResult:
  case Correct(difficulty: Difficulty)
  case Wrong
