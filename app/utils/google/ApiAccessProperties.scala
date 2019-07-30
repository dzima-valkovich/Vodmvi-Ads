package utils.google

import java.util.Properties

import play.api.Configuration
import play.api.ConfigLoader.stringLoader

object ApiAccessProperties {

  private val Oauth2CallbackUriPath = "uri.oauth2.callback"
  private val BaseUriPath = "uri.base"
  private val ScopesPath = "google.api.scopes"

  def apply(): ApiAccessProperties = new ApiAccessProperties()

  def apply(config: Configuration): ApiAccessProperties = {
    val tempProps = apply()
    tempProps.setCallbackUri(config.get(Oauth2CallbackUriPath))
    tempProps.setScopes(config.get(ScopesPath))
    tempProps
  }
}

class ApiAccessProperties extends Properties {

  def getCallbackUri: String = getProperty(ApiAccessProperties.Oauth2CallbackUriPath)

  def setCallbackUri(value: String): Unit = setProperty(ApiAccessProperties.Oauth2CallbackUriPath, value)

  def getScopes: String = getProperty(ApiAccessProperties.ScopesPath)

  def setScopes(value: String): Unit = setProperty(ApiAccessProperties.ScopesPath, value)

  def getBaseUri: String = getProperty(ApiAccessProperties.BaseUriPath)

  def setBaseUri(value: String): Unit = setProperty(ApiAccessProperties.BaseUriPath, value)
}
