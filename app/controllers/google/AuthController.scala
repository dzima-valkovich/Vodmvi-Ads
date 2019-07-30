package controllers.google

import java.math.BigInteger
import java.net.URI
import java.security.SecureRandom

import actors.google.auth.Oauth2Actor.{GetAuthUrlRequest, GetAuthUrlResponse, GetRefreshTokenRequest, GetRefreshTokenResponse}
import akka.actor.ActorRef
import akka.util.Timeout
import com.google.auth.oauth2.{ClientId, UserAuthorizer}
import com.google.common.collect.ImmutableList
import javax.inject.{Inject, Named}
import model.ads.Customer
import play.api.Configuration
import play.api.libs.json.{JsNull, JsValue}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import akka.pattern.ask

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class AuthController @Inject()(
                                @Named("Oauth2Actor") authActorRef: ActorRef,
                                config: Configuration,
                                cc: ControllerComponents
                              )
  extends AbstractController(cc) {

  def auth(): Action[JsValue] = Action(parse.json).async {
    request =>
      implicit val timeout: Timeout = 10.seconds
      val email: Option[String] = (request.body \ "email").getOrElse(JsNull).asOpt[String]
      (authActorRef ? GetAuthUrlRequest(Customer(None, None, email, None)))
        .mapTo[GetAuthUrlResponse]
        .map(message => Ok(message.url.toString))
  }

  def oauth2callback(): Action[AnyContent] = Action.async {
    req =>
      implicit val timeout: Timeout = 10.seconds
      val code = req.headers.get("Raw-Request-URI").get.split('&')(1).replace("code=", "")
      (authActorRef ? GetRefreshTokenRequest(code))
        .mapTo[GetRefreshTokenResponse]
        .map(message => Ok(message.refreshToken))
  }

}
