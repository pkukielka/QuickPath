package models

import play.api.libs.iteratee.Concurrent
import play.api.libs.json.JsValue

case class Position(x: Int, y: Int, z: Int)

sealed abstract class ServerMessage
case class GetUniqueId  ()                                                        extends ServerMessage
case class Join         (username: String, channel: Concurrent.Channel[JsValue])  extends ServerMessage
case class Event        (username: String, event: JsValue)                        extends ServerMessage
case class Move         (username: String, position: Position)                    extends ServerMessage
case class Collision    (username: String, position: Position)                    extends ServerMessage
case class UpdateScore  (username: String, score: Int)                            extends ServerMessage
case class Quit         (username: String)                                        extends ServerMessage
case class PathClosed   (trail: List[Position])                                   extends ServerMessage
case class UpdateSquare (id: Int, size: Double, x: Int, y: Int, color: Int)       extends ServerMessage
case class RemoveSquare (id: Int)                                                 extends ServerMessage
case class IncreaseScore(bonus: Int)                                              extends ServerMessage
case class UpdateSquares()                                                        extends ServerMessage

