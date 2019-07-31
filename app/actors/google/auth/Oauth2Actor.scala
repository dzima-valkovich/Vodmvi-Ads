package actors.google.auth

import java.math.BigInteger
import java.net.{URI, URL}
import java.security.SecureRandom

import actors.google.auth.Oauth2Actor._
import akka.actor.Actor
import javax.inject.Inject
import model.ads.Customer
import play.api.Configuration
import utils.google.UserAuthorizerFactory
import play.api.ConfigLoader.stringLoader

object Oauth2Actor {
  private val BaseUriPath = "uri.base"

  final case class GetAuthUrlRequest(customer: Customer)

  final case class GetAuthUrlResponse(url: URL)

  final case class GetRefreshTokenRequest(code: String)

  final case class GetRefreshTokenResponse(refreshToken: String)

  trait Factory {
    def apply(): Actor
  }

}

class Oauth2Actor @Inject()(userAuth: UserAuthorizerFactory, config: Configuration) extends Actor {
  override def receive: Receive = {
    case GetAuthUrlRequest(customer) =>
      val state: String = new BigInteger(130, new SecureRandom).toString(32)
      val uri = userAuth()
        .getAuthorizationUrl(customer.email.orNull, state, URI.create(config.get(BaseUriPath)))
      sender() ! GetAuthUrlResponse(uri)

    case GetRefreshTokenRequest(code) =>
      val token = userAuth()
        .getCredentialsFromCode(code, URI.create(config.get(BaseUriPath)))
        .getRefreshToken
      sender() ! GetRefreshTokenResponse(token)
  }
}
