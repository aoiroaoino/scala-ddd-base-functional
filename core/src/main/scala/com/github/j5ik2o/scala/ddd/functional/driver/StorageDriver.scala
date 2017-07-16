package com.github.j5ik2o.scala.ddd.functional.driver

import com.github.j5ik2o.scala.ddd.functional.Aggregate

trait StorageDriver[A <: Aggregate, E[_]] {
  type RecordType

  def store(aggregate: A): E[Unit]

  def resolveBy(id: A#IdType): E[Option[A]]

  def deleteById(id: A#IdType): E[Unit]

  protected def convertToRecord(aggregate: A): RecordType

  protected def convertToAggregate(record: Option[RecordType]): Option[A]

}
