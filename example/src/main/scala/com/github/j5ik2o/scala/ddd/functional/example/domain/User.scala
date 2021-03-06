package com.github.j5ik2o.scala.ddd.functional.example.domain

import com.github.j5ik2o.scala.ddd.functional.Aggregate

import scala.reflect.{ classTag, ClassTag }

case class User(id: UserId, name: String) extends Aggregate {
  override type AggregateType = User
  override type IdType        = UserId
  override protected val tag: ClassTag[User] = classTag[User]
}
