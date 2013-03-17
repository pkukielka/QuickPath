package models

import akka.actor.Actor
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.{JsValue, Json}
import scala._
import scala.Some

case class Position(x: Int, y: Int, z: Int)

case class EraseTry(trail: List[Position])

class Player(username: String, channel: Concurrent.Channel[JsValue]) extends Actor {
  var trail = List.empty[Position]
  val allParticles = 50
  val particleSize = 4

  def receive = {
    case e: Event =>
      def getPos(axis: String) = (e.event \ axis).asOpt[Int]

      (getPos("x"), getPos("y"), getPos("z")) match {
        case (Some(x), Some(y), Some(z)) =>
          val pos = Position(x, y, z)

          if (e.username == username) {
            trail = (pos :: trail).take(allParticles)
            if (isColliding(pos)) {
              context.actorFor("../Squares") ! EraseTry(trail)
            }
          }
          else {
            if (isColliding(pos)) {
              trail = trail.take(1)
              channel.push(prepareMsg("collision", e.username, x, y, z))
            }
          }

          channel.push(prepareMsg("move", e.username, x, y, z))

        case _ =>
          play.Logger.warn("Unable to parse message: %s".format(e.toString()))
      }
  }

  def prepareMsg(eventType: String, username: String, x: Int, y: Int, z: Int) =
    Json.obj("type" -> eventType, "username" -> username, "x" -> x, "y" -> y, "z" -> z)

  def distance(p1: Position, p2: Position) =
    Math.sqrt((p1.x - p2.x)^2 + (p1.y - p2.y)^2 + (p1.z - p2.z)^2)

  def isColliding(pos: Position) =
    trail.find(distance(pos, _) <= particleSize).isDefined
}
