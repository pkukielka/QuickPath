package models

import akka.actor.Actor
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.{JsValue, Json}
import scala._

object Player {
  val allParticles = 50
  val particleSize = 4
}

class Player(username: String, channel: Concurrent.Channel[JsValue]) extends Actor {
  var trail = List.empty[Position]
  var score = 0

  def receive = {
    case Collision(senderName, pos) =>
      channel.push(Json.obj("type" -> "Collision", "username" -> senderName, "x" -> pos.x, "y" -> pos.y, "z" -> pos.z))

    case UpdateSquare(id, size, x, y, color) =>
      channel.push(Json.obj("type" -> "UpdateSquare", "id" -> id, "size" -> size, "x" -> x, "y" -> y, "color" -> color))

    case UpdateScore(senderName, score) =>
      channel.push(Json.obj("type" -> "UpdateScore", "username" -> senderName, "score" -> score))

    case RemoveSquare(id) =>
      channel.push(Json.obj("type" -> "RemoveSquare", "id" -> id))

    case IncreaseScore(bonus) =>
      score += bonus
      context.actorFor("../") ! UpdateScore(username, score)

    case Move(senderName: String, pos: Position) =>
      channel.push(Json.obj("type" -> "Move", "username" -> senderName, "x" -> pos.x, "y" -> pos.y, "z" -> pos.z))

      if (senderName == username) {
        if (isColliding(pos)) {
          val closedPath = takeClosedPath(pos)
          if (closedPath.size > 3) {
            context.actorFor("../Squares") ! PathClosed(closedPath)
          }
        }
        trail = (pos :: trail).take(Player.allParticles)
      }
      else {
        if (isColliding(pos)) {
          trail = trail.take(1)
          context.actorFor("../") ! Collision(username, pos)
        }
      }
  }

  def prepareMsg(eventType: String, username: String, x: Int, y: Int, z: Int) =
    Json.obj("type" -> eventType, "username" -> username, "x" -> x, "y" -> y, "z" -> z)

  def distance(p1: Position, p2: Position) =
    Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2))

  def isColliding(p1: Position, p2: Position): Boolean =
    distance(p1, p2) <= Player.particleSize

  def isColliding(pos: Position): Boolean =
    trail.drop(1).find(isColliding(pos, _)).isDefined

  def takeClosedPath(pos: Position) =
    trail.drop(1).takeWhile(!isColliding(pos, _))
}
