package org.salgar.scala.pattern

import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.{Effect, ReplyEffect}
import org.salgar.scala.pattern.BaseActor.{ReportBase, ResponseBase}
import shapeless.{TypeCase, Typeable}

import scala.reflect.ClassTag

object BaseActor {
  implicit def mapActorRef[T: Typeable]: Typeable[ActorRef[T]] =
    new Typeable[ActorRef[T]] {
      private val typT = Typeable[T]

      override def cast(t: Any): Option[ActorRef[T]] = {
        if (t == null) None
        else if (t.isInstanceOf[ActorRef[_]]) {
          val o = t.asInstanceOf[ActorRef[_]]
          for {
            _ <- typT.cast(myClassOf)
          } yield o.asInstanceOf[ActorRef[T]]
        } else None
      }

      override def describe: String = s"ActorRef[${typT.describe}]"
    }

  trait ReportBase[STATE] {
    def replyTo: ActorRef[ResponseBase[STATE]]
  }

  trait ResponseBase[STATE] {
    def state(): STATE
  }

  def myClassOf[T: ClassTag] = implicitly[ClassTag[T]].runtimeClass

  implicit def repsonseBaseIsTypeable[S: Typeable]: Typeable[ResponseBase[S]] =
    new Typeable[ResponseBase[S]] {
      private val typS = Typeable[S]

      override def cast(t: Any): Option[ResponseBase[S]] = {
        if (t == null) None
        else if (t.isInstanceOf[ResponseBase[_]]) {
          val o = t.asInstanceOf[ResponseBase[_]]
          for {
            _ <- typS.cast(o.state)
          } yield o.asInstanceOf[ResponseBase[S]]
        } else None
      }

      override def describe: String = s"ResponseBase[${typS.describe}]"
    }


}

abstract class BaseActor[E: ClassTag, R <: ReportBase[STATE], EVENT, STATE](signal: TypeCase[R]) {
  private val report = signal

  def base[B <: E : ClassTag](cmd: E, stateInternal: STATE)(f: B => ReplyEffect[EVENT, STATE]): ReplyEffect[EVENT, STATE] = {
    cmd match {
      case report(replyTo) =>
        Effect.reply(replyTo.replyTo)(new ResponseBase[STATE] {
          override def state() = stateInternal
        })

      case m: B => f(m)
    }
  }
}