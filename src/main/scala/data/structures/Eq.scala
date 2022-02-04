package data.structures

trait Eq[A]:
  extension (l: A)
    def eq(r: A): Boolean
    def ==(r: A) = l.eq (r)
