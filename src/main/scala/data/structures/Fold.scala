package data.structures

trait Fold[A]:
  extension (a: A)
    def fold[B](b: A): B
