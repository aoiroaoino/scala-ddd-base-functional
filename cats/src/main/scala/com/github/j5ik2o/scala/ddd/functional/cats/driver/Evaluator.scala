package com.github.j5ik2o.scala.ddd.functional.cats.driver

import cats.free.Free
import cats.{ ~>, Monad }
import com.github.j5ik2o.scala.ddd.functional.{ Aggregate, AggregateRepositoryDSL }
import com.github.j5ik2o.scala.ddd.functional.AggregateRepositoryDSL.{ Delete, ResolveById, Store }
import com.github.j5ik2o.scala.ddd.functional.driver.StorageDriver

trait Evaluator[M <: Aggregate, E[_]] extends (AggregateRepositoryDSL ~> E) {

  implicit val driver: StorageDriver[M, E]

  override def apply[A](fa: AggregateRepositoryDSL[A]): E[A] = fa match {
    case Store(aggregate) =>
      driver
        .store(aggregate.asInstanceOf[M])
        .asInstanceOf[E[A]]
    case ResolveById(id) =>
      driver
        .resolveBy(id.asInstanceOf[M#IdType])
        .asInstanceOf[E[A]]
    case Delete(id) =>
      driver
        .deleteById(id.asInstanceOf[M#IdType])
        .asInstanceOf[E[A]]
  }

  def run[A](program: Free[AggregateRepositoryDSL, A])(implicit M: Monad[E]): E[A] =
    program.foldMap(this)
}

object Evaluator {

  def apply[M <: Aggregate, E[_]]()(implicit driver: StorageDriver[M, E]): Evaluator[M, E] = Default()

  private case class Default[M <: Aggregate, E[_]]()(implicit val driver: StorageDriver[M, E]) extends Evaluator[M, E]

}
