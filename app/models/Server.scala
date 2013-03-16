package models

import akka.actor.Actor
import play.api.libs.iteratee.{Enumerator, Concurrent}
import play.api.libs.json._

sealed abstract class ServerMessage
case class Event     (event:      JsValue)             extends ServerMessage
case class Join      (username:   String)              extends ServerMessage
case class Connected (enumerator: Enumerator[JsValue]) extends ServerMessage

class Server extends Actor {
  var players = Map.empty[String, Concurrent.Channel[JsValue]]

  def receive = {
    case Join(username) =>
      // Concurrent.unicast returns iteratee.Enumerator[E] to the sender
      sender ! Connected(Concurrent.unicast[JsValue](
        onStart = channel => players = players + (username -> channel),
        onComplete = players = players - username
      ))

    case Event(e) => {
      players.values.foreach(_.push(e))
    }
  }
}
