package models

import akka.actor.Actor
import java.awt.{Rectangle, Polygon}
import util.Random

case class GrowingSquare(id: Int) {
  val color         = Random.nextInt(4)
  val x             = 100 - Random.nextInt(200)
  val y             = 50  - Random.nextInt(100)
  val minSize       = 1 + Random.nextInt(5)
  val maxSize       = minSize + 1 + Random.nextInt(5)
  var size: Double  = minSize
  var delta         = 0.1

  def getSize = size.toInt + Player.particleSize
  def getModel: Rectangle = new Rectangle(x - (getSize / 2), y - (getSize / 2), getSize, getSize)
  def getMaxModel: Rectangle = new Rectangle(x - (maxSize / 2), y - (maxSize / 2), maxSize, maxSize)
  def update {
    if (size > maxSize || size < minSize) {
      delta = -delta
    }
    size += delta
  }
}

class Squares extends Actor {
  var squares = List.empty[GrowingSquare]
  var id: Int = 0

  def receive = {
    case PathClosed(trail) =>
      val polygon = new Polygon(trail.map(_.x).toArray, trail.map(_.y).toArray, trail.size)
      val closedSquares = squares.filter { square =>
        polygon.contains(square.getModel)
      }

      if (closedSquares.groupBy(_.color).size == 1) {
        squares = squares filterNot(closedSquares contains)
        sender ! IncreaseScore((closedSquares.foldLeft(0)(_ + _.getSize)) * Math.pow(closedSquares.size, 2).toInt)
        closedSquares.foreach { square =>
          context.actorFor("../") ! RemoveSquare(square.id)
        }
      }

    case Move(senderName, pos) =>
      if (squares.exists(_.getModel.contains(pos.x, pos.y))) {
        context.actorFor("../") ! Collision(senderName, pos)
      }

    case UpdateSquares() =>
      if (squares.size < 30) {
        val square = new GrowingSquare(id)

        if (squares.find(_.getModel.intersects(square.getModel)).isEmpty)
          squares = square :: squares

        id += 1
      }
      squares.foreach { square =>
        square.update
        context.actorFor("../") ! UpdateSquare(square.id, square.size, square.x, square.y, square.color)
      }
  }
}
