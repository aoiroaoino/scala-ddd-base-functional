package com.github.j5ik2o.scala.ddd.functional.scalaz

import com.github.j5ik2o.scala.ddd.functional.{ AggregateReader, AggregateRepositoryDSL }

import scalaz.Free

trait FreeIOReadFeature extends FreeIOBaseFeature with AggregateReader {
  import AggregateRepositoryDSL._

  override type SingleResultType[A] = Option[A]

  override def resolveBy(
      id: AggregateIdType
  ): DSL[SingleResultType[AggregateType]] =
    Free.liftF[AggregateRepositoryDSL, SingleResultType[AggregateType]](ResolveById(id))

}
