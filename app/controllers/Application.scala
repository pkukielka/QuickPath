package controllers

import play.api._
import libs.concurrent.Promise
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.iteratee._

object Application extends Controller {
	val hubEnum = Enumerator.imperative[JsValue]()
	val hub = Concurrent.hub[JsValue](hubEnum)

  def index = Action {
    Ok(views.html.index())
  }

  def stream = WebSocket.async[JsValue] {
  	request =>
      val out = hub.getPatchCord()
      val in = Iteratee.foreach[JsValue] (hubEnum push _)

  		Promise.pure((in, out))
	}
}