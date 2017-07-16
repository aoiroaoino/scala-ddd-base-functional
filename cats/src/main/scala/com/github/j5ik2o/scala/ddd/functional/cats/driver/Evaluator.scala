package com.github.j5ik2o.scala.ddd.functional.cats.driver

import cats.free.Free
import cats.{ ~>, Monad }
import com.github.j5ik2o.scala.ddd.functional.{ Aggregate, AggregateRepositoryDSL }
import com.github.j5ik2o.scala.ddd.functional.AggregateRepositoryDSL.{ Delete, ResolveById, Store }
import com.github.j5ik2o.scala.ddd.functional.driver.StorageDriver

trait EvaluateFeature[M <: Aggregate, E[_]] {

  type DSL[A] <: AggregateRepositoryDSL[A]

  implicit val driver: StorageDriver[M, E]

  protected def storePF[A]: PartialFunction[DSL[A], E[A]] = {
    case Store(aggregate) =>
      driver.store(aggregate.asInstanceOf[M]).asInstanceOf[E[A]]
  }

  protected def resolvePF[A]: PartialFunction[DSL[A], E[A]] = {
    case ResolveById(id) =>
      driver.resolveBy(id.asInstanceOf[M#IdType]).asInstanceOf[E[A]]
  }

}

trait EvaluateHardDeleteFeature[M <: Aggregate, E[_]] { this: EvaluateFeature[M, E] =>

  protected def deletePF[A]: PartialFunction[DSL[A], E[A]] = {
    case Delete(id) =>
      driver.deleteById(id.asInstanceOf[M#IdType]).asInstanceOf[E[A]]
  }

}

trait Evaluator[M <: Aggregate, E[_]]
    extends (AggregateRepositoryDSL ~> E)
    with EvaluateFeature[M, E]
    with EvaluateHardDeleteFeature[M, E] {

  def run[A](program: Free[AggregateRepositoryDSL, A])(implicit M: Monad[E]): E[A] =
    program.foldMap(this)

}

object Evaluator {

  def apply[M <: Aggregate, E[_]]()(implicit driver: StorageDriver[M, E]): Evaluator[M, E] = Default()

  private case class Default[M <: Aggregate, E[_]]()(implicit val driver: StorageDriver[M, E]) extends Evaluator[M, E] {
    override type DSL[A] = AggregateRepositoryDSL[A]
    override def apply[A](fa: DSL[A]): E[A] =
      storePF[A].orElse(resolvePF[A].orElse(deletePF[A]))(fa)
  }

}
