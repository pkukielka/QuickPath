package models

import akka.actor.{ActorRef, Actor, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._

import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee._
import play.api.libs.json._
import play.libs.Akka
import javax.print.attribute.standard.MediaSize.Other

object Server {
  implicit val timeout = Timeout(1 second)
  lazy val serv = Akka.system.actorOf(Props[Server])

  def join(): scala.concurrent.Future[(Iteratee[JsValue,_], Enumerator[JsValue])] = {
    (serv ? GetUniqueId()).map {
      case userId: Int =>
        val username   = userId.toString
        val enumerator = Concurrent.unicast[JsValue](serv ! Join(username, _))
        val iteratee   = Iteratee.foreach[JsValue](serv ! Event(username, _)).mapDone(_ => serv ! Quit(username))
        (iteratee, enumerator)

      case _ =>
        val iteratee   = Done[JsValue,Unit]((),Input.EOF)
        val enumerator =
          Enumerator[JsValue](Json.obj("error" -> "Server cannot create unique user name")) >>>
          Enumerator.enumInput(Input.EOF)
        (iteratee,enumerator)
    }
  }
}

class Server extends Actor {
  var players = Map.empty[String, ActorRef]
  val squares = context.actorOf(Props[Squares], name = "Squares")
  var id: Int = 0

  Akka.system.scheduler.schedule(0 seconds, 100 milliseconds) {
    squares ! UpdateSquares()
  }

  def receive = {
    case GetUniqueId() =>
      sender ! {id += 1; id}

    case Join(username, channel) =>
      val playerActorRef = context.actorOf(Props(new Player(username, channel)), name = username)
      players = players + (username -> playerActorRef)

    case Event(username, event) =>
      def getPos(axis: String) = (event \ axis).asOpt[Int]

      (getPos("x"), getPos("y"), getPos("z")) match {
        case (Some(x), Some(y), Some(z)) =>
          val move = Move(username, Position(x, y, z))
          players.values.foreach(_ ! move)
          squares ! move

        case _ =>
          play.Logger.warn("Unable to parse message %s from user %s".format(event.toString(), username))
      }

    case Quit(username) =>
      players = players - username

    case otherMsg =>
      players.values.foreach(_ ! otherMsg)
  }
}
