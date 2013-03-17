package models

import akka.actor.{ActorRef, Actor, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._

import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee._
import play.api.libs.json._
import play.libs.Akka

sealed abstract class ServerMessage
case class GetUniqueId  ()                                                       extends ServerMessage
case class Join         (username: String, channel: Concurrent.Channel[JsValue]) extends ServerMessage
case class Event        (username: String, event: JsValue)                       extends ServerMessage
case class Quit         (username: String)                                       extends ServerMessage

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
  var id: Int = 0

  def receive = {
    case GetUniqueId() =>
      sender ! {id += 1; id}

    case Join(username, channel) =>
      val playerActorRef = context.actorOf(Props(new Player(username, channel)), name = username)
      players = players + (username -> playerActorRef)

    case e: Event =>
      players.values.foreach(_ ! e)

    case Quit(username) =>
      players = players - username
  }
}
