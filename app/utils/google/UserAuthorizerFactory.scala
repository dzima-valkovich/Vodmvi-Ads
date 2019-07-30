package utils.google

import java.net.URI

import com.google.auth.oauth2.{ClientId, UserAuthorizer}
import com.google.common.collect.ImmutableList
import javax.inject.Inject
import play.api.Configuration
import play.api.ConfigLoader.stringLoader

object UserAuthorizerFactory {

  private val ClientIdPath = "api.googleads.clientId"
  private val ClientSecretPath = "api.googleads.clientSecret"
  private val Oauth2CallbackUriPath = "uri.oauth2.callback"
  private val ScopesPath = "google.api.scopes"
}

class UserAuthorizerFactory @Inject()(config: Configuration) {

  private lazy val userAuth = {
    import collection.JavaConverters._
    val scopes = config
      .get(UserAuthorizerFactory.ScopesPath)
      .split("\n")
      .toList
      .asJava


    UserAuthorizer
      .newBuilder
      .setClientId(ClientId.of(config.get(UserAuthorizerFactory.ClientIdPath), config.get(UserAuthorizerFactory.ClientSecretPath)))
      .setScopes(scopes)
      .setCallbackUri(URI.create(config.get(UserAuthorizerFactory.Oauth2CallbackUriPath)))
      .build
  }

  def apply(): UserAuthorizer = userAuth
}
