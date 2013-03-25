package controllers

import play.api._
import libs.concurrent.Promise
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.iteratee._

object Application extends Controller {
  def index = Action { implicit request =>
    Ok(views.html.index())
  }

  def stream = WebSocket.async[JsValue] { implicit request =>
    models.Server.join()
	}
}