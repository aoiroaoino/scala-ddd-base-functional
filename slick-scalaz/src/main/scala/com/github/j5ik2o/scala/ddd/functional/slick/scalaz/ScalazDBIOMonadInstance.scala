package com.github.j5ik2o.scala.ddd.functional.slick.scalaz

import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext
import scalaz.Monad

trait ScalazDBIOMonadInstance {
  val profile: JdbcProfile
  import profile.api._

  implicit def dbIOMonad(implicit ec: ExecutionContext) = new Monad[DBIO] {

    override def bind[A, B](fa: profile.api.DBIO[A])(f: (A) => profile.api.DBIO[B]): profile.api.DBIO[B] =
      fa.flatMap(f)

    override def point[A](a: => A): profile.api.DBIO[A] = DBIO.successful(a)

  }

}

case class ScalazDBIOImplicits(profile: JdbcProfile) extends ScalazDBIOMonadInstance
