package com.github.j5ik2o.scala.ddd.functional.example.domain.scalaz

import com.github.j5ik2o.scala.ddd.functional.example.domain.{ User, UserId }
import com.github.j5ik2o.scala.ddd.functional.scalaz.{ FreeIODeleteFeature, FreeIORepositoryFeature }

object UserRepository extends FreeIORepositoryFeature with FreeIODeleteFeature {
  override type IdValueType     = Long
  override type AggregateIdType = UserId
  override type AggregateType   = User
}
