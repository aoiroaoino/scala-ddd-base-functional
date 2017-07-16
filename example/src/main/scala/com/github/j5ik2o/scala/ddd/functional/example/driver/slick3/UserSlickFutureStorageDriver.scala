package com.github.j5ik2o.scala.ddd.functional.example.driver.slick3

import cats.data.Kleisli
import com.github.j5ik2o.scala.ddd.functional.driver.StorageDriver
import com.github.j5ik2o.scala.ddd.functional.example.domain.{ User, UserId }
import com.github.j5ik2o.scala.ddd.functional.example.driver.slick3.UserSlickFutureStorageDriver.EvalType
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

object UserSlickFutureStorageDriver {
  type EvalType[A] = Kleisli[Future, ExecutionContext, A]
}

case class UserSlickFutureStorageDriver(profile: JdbcProfile, db: JdbcProfile#Backend#Database)
    extends StorageDriver[User, UserSlickFutureStorageDriver.EvalType]
    with UserDaoComponent {
  override type RecordType = UserRecord

  protected val dao = UserDao

  import profile.api._

  override def store(aggregate: User): EvalType[Unit] = Kleisli { implicit ec =>
    val record = convertToRecord(aggregate)
    val action = (for {
      n <- dao.filter(_.id === aggregate.id.value).update(record)
      _ <- if (n == 0) dao.forceInsert(record) else DBIO.successful(n)
    } yield ()).transactionally
    db.run(action)
  }

  override def resolveBy(id: UserId): EvalType[Option[User]] = Kleisli { implicit ec =>
    val action =
      dao
        .filter(_.id === id.value)
        .result
        .headOption
        .map(e => convertToAggregate(e))
    db.run(action)
  }

  override def deleteById(id: UserId): EvalType[Unit] = Kleisli { implicit ec =>
    val action = dao.filter(_.id === id.value).delete
    db.run(
      action
        .flatMap { v =>
          (if (v == 1)
             DBIO.successful(())
           else
             DBIO.failed(new Exception())): DBIO[Unit]
        }
    )
  }

  override protected def convertToRecord(aggregate: User): UserRecord =
    UserRecord(id = aggregate.id.value, name = aggregate.name)

  override protected def convertToAggregate(record: Option[UserRecord]): Option[User] =
    record.map(e => User(id = UserId(e.id), name = e.name))
}
