package com.github.j5ik2o.scala.ddd.functional.scalaz.driver

import com.github.j5ik2o.scala.ddd.functional.{ Aggregate, AggregateRepositoryDSL }
import com.github.j5ik2o.scala.ddd.functional.driver.{ EvaluateFeature, EvaluateHardDeleteFeature, StorageDriver }

import scalaz.{ ~>, Free, Monad }

trait Evaluator[M <: Aggregate, E[_]]
    extends (AggregateRepositoryDSL ~> E)
    with EvaluateFeature[M, E]
    with EvaluateHardDeleteFeature[M, E] {

  def run[A](program: Free[AggregateRepositoryDSL, A])(implicit M: Monad[E]): E[A] =
    program.foldMap(this)

}

object Evaluator {

  def apply[M <: Aggregate, E[_]]()(implicit driver: StorageDriver[M, E]): Evaluator[M, E] = Default()

  private case class Default[M <: Aggregate, E[_]]()(implicit val driver: StorageDriver[M, E])
      extends Evaluator[M, E] {
    override type DSL[A] = AggregateRepositoryDSL[A]

    override def apply[A](fa: DSL[A]): E[A] =
      storePF[A].orElse(resolvePF[A].orElse(deletePF[A]))(fa)
  }

}
