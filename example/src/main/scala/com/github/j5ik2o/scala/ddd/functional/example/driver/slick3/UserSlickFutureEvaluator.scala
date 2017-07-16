package com.github.j5ik2o.scala.ddd.functional.example.driver.slick3

import cats.data.Kleisli
import com.github.j5ik2o.scala.ddd.functional.cats.driver.FreeIOEvaluator

import scala.concurrent.{ExecutionContext, Future}

case class UserSlickFutureEvaluator(override val driver: UserSlickFutureStorageDriver) extends FreeIOEvaluator {
  override type EvalType[A] = Kleisli[Future, ExecutionContext, A]
  override type DriverType = UserSlickFutureStorageDriver
  override type IdValueType = driver.AggregateIdType#IdValueType
  override type AggregateIdType = driver.AggregateIdType
  override type AggregateType = driver.AggregateType
}
