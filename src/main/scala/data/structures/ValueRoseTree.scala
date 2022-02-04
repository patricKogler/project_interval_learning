package data.structures

import cats.Foldable
import data.structures.ValueRoseTree.{Branch, Leaf}


enum ValueRoseTree[A, B]:
  case Branch(meta: A, branches: List[ValueRoseTree[A, B]], values: List[B])
  case Leaf(meta: A, values: List[B])

extension[A, B] (vrt: ValueRoseTree[A, B])
  def meta = vrt match {
    case ValueRoseTree.Branch(meta, branches, values) => meta
    case ValueRoseTree.Leaf(meta, values) => meta
  }
  def map[C](f: B => C): ValueRoseTree[A, C] = vrt match {
    case Branch(name, branches, values) => Branch(name, branches.map(_.map(f)), values.map(f))
    case Leaf(name, values) => Leaf(name, values.map(f))
  }
  def mapMeta[C](f: A => C): ValueRoseTree[C, B] = vrt match {
    case ValueRoseTree.Branch(meta, branches, values) => Branch(f(meta), branches.map(_.mapMeta(f)), values)
    case ValueRoseTree.Leaf(meta, values) => Leaf(f(meta), values)
  }
  def filter(f: B => Boolean): ValueRoseTree[A, B] = vrt match {
    case ValueRoseTree.Branch(name, branches, values) => Branch(name, branches.map(_.filter(f)), values.filter(f))
    case ValueRoseTree.Leaf(name, values) => Leaf(name, values.filter(f))
  }
  def dualSorted(using ordA: Ordering[A])(using ordB: Ordering[B]) = vrt match {
    case ValueRoseTree.Branch(meta, branches, values) => Branch(meta, branches.sortBy(_.meta), values.sorted)
    case ValueRoseTree.Leaf(meta, values) => Leaf(meta, values.sorted)
  }
  def sorted[C](using ordB: Ordering[B])(using ordC: Ordering[C])(using fold: Fold[B, C]) = ???
