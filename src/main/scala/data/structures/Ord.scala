package data.structures

trait Ord[A] extends Eq[A] :
  extension (l: A)
    def compare(r: A): Sorting
    def <(r: A) = l.compare(r) == -1
    def >(r: A) = l.compare(r) == 1
    def <=(r: A) = l < r || l == r
    def >=(r: A) = l > r || l == r
