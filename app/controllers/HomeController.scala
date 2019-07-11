package controllers

import play.api.mvc.{Action, _}
import views.html

class HomeController extends Controller {

  def index(): Action[AnyContent] = Action {
    Ok(html.index.render("Your new application is readyyyyyy."))
  }
}
