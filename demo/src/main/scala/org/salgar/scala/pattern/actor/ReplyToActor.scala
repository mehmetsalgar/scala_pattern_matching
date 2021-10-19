package org.salgar.scala.pattern.actor

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import org.salgar.scala.pattern.BaseActor
import org.salgar.scala.pattern.BaseActor.ResponseBase
import org.salgar.scala.pattern.actor.Actor.{ProcessCommand, onReport}
import org.salgar.scala.pattern.model.UseCaseKey

object ReplyToActor {
  def apply(): Behavior[Command] = Behaviors
    .setup[Command] { context =>
      val responseWrapper: ActorRef[BaseActor.ResponseBase[Actor.State]] =
        context.messageAdapter(response => WrappedReportStateResponse(response))

      Behaviors.receivePartial[Command] {
        case (ctx, Ping(m, ref)) =>
          spawnActor(ctx) ! onReport(UseCaseKey("123"), ref)
          Behaviors.same

        case (ctx, WrappedReportStateResponse(response)) =>
          ctx.log.debug("We are processing WrappedReportStateResponse(response: {}", response.toString)
          Behaviors.same
      }
    }

  def spawnActor(ctx: ActorContext[Command]): ActorRef[ProcessCommand] = {
    val actor = new Actor()
    ctx.spawn(actor.apply(), "actor")
  }

  sealed trait Command

  final case class Ping(message: String, actorRef: ActorRef[ResponseBase[Actor.State]]) extends Command

  //MessageAdapter
  private final case class WrappedReportStateResponse(reportResponse: ResponseBase[Actor.State])
    extends Command
}
