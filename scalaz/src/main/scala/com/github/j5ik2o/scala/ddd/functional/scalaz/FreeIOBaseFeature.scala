package com.github.j5ik2o.scala.ddd.functional.scalaz

import com.github.j5ik2o.scala.ddd.functional.{ AggregateIO, AggregateRepositoryDSL }

import scalaz.Free

trait FreeIOBaseFeature extends AggregateIO {
  type DSL[A] = Free[AggregateRepositoryDSL, A]
}
