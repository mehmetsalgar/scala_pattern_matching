package org.salgar.scala.pattern.actor

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.persistence.typed.scaladsl.{Effect, ReplyEffect}
import org.salgar.scala.pattern.BaseActor
import org.salgar.scala.pattern.BaseActor.ResponseBase
import org.salgar.scala.pattern.actor.Actor._
import org.salgar.scala.pattern.model.UseCaseKey
import shapeless.TypeCase

object Actor {
  sealed trait ProcessCommand

  sealed trait PersistEvent

  trait State

  final case class INITIAL() extends State

  final case class onReport(key: UseCaseKey, replyTo: ActorRef[ResponseBase[State]]) extends ProcessCommand with BaseActor.ReportBase[State]
}

class Actor extends BaseActor[ProcessCommand, onReport, PersistEvent, State](TypeCase[onReport]) {
  def apply(): Behavior[ProcessCommand] = {
    Behaviors.setup[ProcessCommand] { context =>
      Behaviors.receivePartial[ProcessCommand] {
        case _command@_ =>
          commandHandler(_command._2, INITIAL())
          Behaviors.same
      }
    }
  }

  def commandHandler(cmd: ProcessCommand, state: State): ReplyEffect[PersistEvent, State] = {
    state match {
      case Actor.INITIAL() =>
        base[ProcessCommand](cmd, state) {
          case _ =>
            Effect.unhandled.thenNoReply()
        }
      case _ =>
        Effect.unhandled.thenNoReply()
    }
  }
}