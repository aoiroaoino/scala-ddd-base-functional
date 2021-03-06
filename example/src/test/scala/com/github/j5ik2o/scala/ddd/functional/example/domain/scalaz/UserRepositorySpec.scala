package com.github.j5ik2o.scala.ddd.functional.example.domain.scalaz

import com.github.j5ik2o.scala.ddd.functional.example.domain.{ User, UserId }
import com.github.j5ik2o.scala.ddd.functional.example.driver.scalaz.{
  UserSkinnyORMFutureStorageDriver,
  UserSlickDBIOStorageDriver,
  UserSlickFutureStorageDriver
}
import com.github.j5ik2o.scala.ddd.functional.scalaz.driver.Evaluator
import com.github.j5ik2o.scala.ddd.functional.skinnyorm.{ SkinnyORMFutureIOContext, SkinnyORMSpecSupport }
import com.github.j5ik2o.scala.ddd.functional.slick.Slick3SpecSupport
import com.github.j5ik2o.scala.ddd.functional.slick.scalaz.ScalazDBIOImplicits
import com.github.j5ik2o.scala.ddd.functional.slick.test.FlywayWithMySQLSpecSupport
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ BeforeAndAfterAll, FreeSpec }
import scalikejdbc.AutoSession

import scalaz._
import Scalaz._

class UserRepositorySpec
    extends FreeSpec
    with BeforeAndAfterAll
    with SkinnyORMSpecSupport
    with Slick3SpecSupport
    with FlywayWithMySQLSpecSupport
    with ScalaFutures {

  "UserRepository" - {
    "should be able to store and resolve" - {
      "when DBIO of Slick" in {
        val driver = UserSlickDBIOStorageDriver(dbConfig.profile, dbConfig.db)
        import driver._
        val evaluator = Evaluator[User, driver.EvalType]()
        val program = for {
          _  <- UserRepository.store(User(UserId(1), "kato"))
          r1 <- UserRepository.resolveBy(UserId(1))
          _  <- UserRepository.deleteById(UserId(1))
          r2 <- UserRepository.resolveBy(UserId(1))
        } yield (r1, r2)
        val implicits = ScalazDBIOImplicits(driver.profile)
        import implicits._
        val dbio   = evaluator.run(program).run(ec)
        val future = driver.db.run(dbio)
        val result = future.futureValue
        println(result)
      }
      "when Future of Slick" in {
        implicit val driver = UserSlickFutureStorageDriver(dbConfig.profile, dbConfig.db)
        val evaluator       = Evaluator[User, UserSlickFutureStorageDriver.EvalType]()
        val program = for {
          _  <- UserRepository.store(User(UserId(2), "kato"))
          r1 <- UserRepository.resolveBy(UserId(2))
          _  <- UserRepository.deleteById(UserId(2))
          r2 <- UserRepository.resolveBy(UserId(2))
        } yield (r1, r2)
        val future = evaluator.run(program).run(ec)
        val result = future.futureValue
        println(result)
      }
      "when Future of Skinny" in {
        implicit val driver = UserSkinnyORMFutureStorageDriver()
        val evaluator       = Evaluator[User, UserSkinnyORMFutureStorageDriver.EvalType]()
        val program = for {
          _  <- UserRepository.store(User(UserId(3), "kato"))
          r1 <- UserRepository.resolveBy(UserId(3))
          _  <- UserRepository.deleteById(UserId(3))
          r2 <- UserRepository.resolveBy(UserId(3))
        } yield (r1, r2)
        val ctx    = SkinnyORMFutureIOContext(ec, AutoSession)
        val future = evaluator.run(program).run(ctx)
        val result = future.futureValue
        println(result)
      }
    }
  }

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    startSlick()
    startSkinnyORM()
  }

  override protected def afterAll(): Unit = {
    stopSkinnyORM()
    stopSlick()
    super.afterAll()
  }
}
