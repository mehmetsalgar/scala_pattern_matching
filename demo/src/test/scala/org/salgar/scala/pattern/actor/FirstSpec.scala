package org.salgar.scala.pattern.actor

import akka.actor.testkit.typed.scaladsl.{ActorTestKit, ScalaTestWithActorTestKit}
import akka.actor.typed.scaladsl.AskPattern.Askable
import org.salgar.scala.pattern.BaseActor
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}


class FirstSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  "Patten Matching" must {
    "behave correctly" in {
      val actorTestKit = ActorTestKit()
      val replyToRef = actorTestKit.spawn(ReplyToActor(), "replyTo")

      val future: Future[BaseActor.ResponseBase[Actor.State]] = replyToRef.ask[BaseActor.ResponseBase[Actor.State]](ref =>
        ReplyToActor.Ping("123", ref)
      )(30.seconds, system.scheduler)

      Await.result(future, 60.seconds)
    }
  }
}