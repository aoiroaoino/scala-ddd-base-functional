package com.github.j5ik2o.scala.ddd.functional.driver

import com.github.j5ik2o.scala.ddd.functional.Aggregate
import com.github.j5ik2o.scala.ddd.functional.AggregateRepositoryDSL.Delete

/**
  * Created by j5ik2o on 2017/07/16.
  */
trait EvaluateHardDeleteFeature[M <: Aggregate, E[_]] { this: EvaluateFeature[M, E] =>

  protected def deletePF[A]: PartialFunction[DSL[A], E[A]] = {
    case Delete(id) =>
      driver.deleteById(id.asInstanceOf[M#IdType]).asInstanceOf[E[A]]
  }

}
