package com.github.j5ik2o.scala.ddd.functional.example.driver.slick3

import cats.data.Kleisli
import com.github.j5ik2o.scala.ddd.functional.driver.StorageDriver
import com.github.j5ik2o.scala.ddd.functional.example.domain.{ User, UserId }
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

case class UserSlickDBIOStorageDriver(profile: JdbcProfile, db: JdbcProfile#Backend#Database) extends UserDaoComponent {

  type EvalType[A] = Kleisli[profile.api.DBIO, ExecutionContext, A]

  implicit object InternalDriver extends StorageDriver[User, EvalType] {
    override type RecordType = UserRecord

    protected val dao = UserDao

    import profile.api._

    override protected def convertToRecord(aggregate: User): UserRecord =
      UserRecord(id = aggregate.id.value, name = aggregate.name)

    override protected def convertToAggregate(record: Option[UserRecord]): Option[User] =
      record.map(e => User(id = UserId(e.id), name = e.name))

    override def store(aggregate: User): EvalType[Unit] = Kleisli { implicit ec =>
      val record = convertToRecord(aggregate)
      val action = (for {
        n <- dao.filter(_.id === aggregate.id.value).update(record)
        _ <- if (n == 0) dao.forceInsert(record) else DBIO.successful(n)
      } yield ()).transactionally
      action.asInstanceOf[DBIO[Unit]]
    }

    override def resolveBy(id: UserId): EvalType[Option[User]] = Kleisli { implicit ec =>
      val action =
        dao
          .filter(_.id === id.value)
          .result
          .headOption
          .map(convertToAggregate)
      action.asInstanceOf[DBIO[Option[User]]]
    }

    override def deleteById(id: UserId): EvalType[Unit] = Kleisli { implicit ec =>
      val action = dao.filter(_.id === id.value).delete
      action
        .flatMap { v =>
          if (v == 1)
            DBIO.successful(())
          else
            DBIO.failed(new Exception())
        }
        .asInstanceOf[DBIO[Unit]]
    }
  }
}
