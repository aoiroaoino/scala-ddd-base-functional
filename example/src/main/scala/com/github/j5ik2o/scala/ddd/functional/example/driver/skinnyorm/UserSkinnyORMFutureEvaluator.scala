package com.github.j5ik2o.scala.ddd.functional.example.driver.skinnyorm

import cats.data.Kleisli
import com.github.j5ik2o.scala.ddd.functional.cats.driver.FreeIOEvaluator
import com.github.j5ik2o.scala.ddd.functional.skinnyorm.SkinnyORMFutureIOContext

import scala.concurrent.Future

case class UserSkinnyORMFutureEvaluator(override val driver: UserSkinnyORMFutureStorageDriver) extends FreeIOEvaluator {
  override type EvalType[A] = Kleisli[Future, SkinnyORMFutureIOContext, A]
  override type DriverType = UserSkinnyORMFutureStorageDriver
  override type IdValueType = driver.AggregateIdType#IdValueType
  override type AggregateIdType = driver.AggregateIdType
  override type AggregateType = driver.AggregateType
}
