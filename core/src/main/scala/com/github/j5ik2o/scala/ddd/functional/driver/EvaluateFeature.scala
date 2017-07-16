package com.github.j5ik2o.scala.ddd.functional.driver

import com.github.j5ik2o.scala.ddd.functional.AggregateRepositoryDSL.{ ResolveById, Store }
import com.github.j5ik2o.scala.ddd.functional.{ Aggregate, AggregateRepositoryDSL }

/**
  * Created by j5ik2o on 2017/07/16.
  */
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
