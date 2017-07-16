package com.github.j5ik2o.scala.ddd.functional.example.driver.scalaz

import com.github.j5ik2o.scala.ddd.functional.driver.StorageDriver
import com.github.j5ik2o.scala.ddd.functional.example.domain.{ User, UserId }
import com.github.j5ik2o.scala.ddd.functional.example.driver.skinnyorm.{ UserDao, UserRecord }
import com.github.j5ik2o.scala.ddd.functional.skinnyorm.SkinnyORMFutureIOContext
import scalikejdbc.DB
import skinny.orm.SkinnyCRUDMapperWithId

import scala.concurrent.Future
import scalaz.Kleisli

object UserSkinnyORMFutureStorageDriver {
  type EvalType[A] = Kleisli[Future, SkinnyORMFutureIOContext, A]
}

case class UserSkinnyORMFutureStorageDriver() extends StorageDriver[User, UserSkinnyORMFutureStorageDriver.EvalType] {
  import UserSkinnyORMFutureStorageDriver._
  override type RecordType = UserRecord

  protected val dao: SkinnyCRUDMapperWithId[Long, RecordType] = UserDao

  private def withContext[A](
      body: SkinnyORMFutureIOContext => Future[A]
  ): EvalType[A] =
    Kleisli[Future, SkinnyORMFutureIOContext, A](body)

  override protected def convertToRecord(aggregate: User): UserRecord =
    UserRecord(id = aggregate.id.value, name = aggregate.name)

  override protected def convertToAggregate(record: Option[UserRecord]): Option[User] =
    record.map(e => User(id = UserId(e.id), name = e.name))

  protected def toNamedValues(record: UserRecord): Seq[(Symbol, Any)] = Seq(
    'name -> record.name
  )

  override def store(aggregate: User): EvalType[Unit] = withContext[Unit] { ctx =>
    implicit val ec = ctx.ec
    Future {
      DB.localTx { dbSession =>
        val namedValues = toNamedValues(convertToRecord(aggregate))
        val result      = dao.updateById(aggregate.id.value).withAttributes(namedValues: _*)(dbSession)
        if (result > 0) ()
        else {
          dao.createWithAttributes(('id -> aggregate.id.value) +: namedValues: _*)(dbSession)
          ()
        }
      }
    }
  }

  override def resolveBy(id: UserId): EvalType[Option[User]] =
    withContext[Option[User]] { ctx =>
      implicit val ec = ctx.ec
      Future {
        convertToAggregate(dao.findById(id.value))
      }
    }

  override def deleteById(id: UserId): EvalType[Unit] =
    withContext[Unit] { ctx =>
      implicit val ec = ctx.ec
      Future {
        dao.deleteById(id.value)
        ()
      }
    }

}
