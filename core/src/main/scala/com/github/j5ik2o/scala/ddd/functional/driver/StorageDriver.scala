package com.github.j5ik2o.scala.ddd.functional.driver

trait StorageDriver extends AggregateRepository with AggregateDeletable {
  type RecordType

  protected def convertToRecord(aggregate: AggregateType): RecordType

  protected def convertToAggregate(record: Option[RecordType]): Option[AggregateType]
}
