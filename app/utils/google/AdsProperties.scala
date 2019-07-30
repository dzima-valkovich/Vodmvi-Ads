package utils.google

import java.util.Properties

import play.api.Configuration
import play.api.ConfigLoader.stringLoader

object AdsProperties {
  private val ClientIdPath = "api.googleads.clientId"
  private val DeveloperTokenPath = "api.googleads.developerToken"
  private val ClientSecretPath = "api.googleads.clientSecret"

  private val RefreshTokenPath = "api.googleads.refreshToken"
  private val LoginCustomerIdPath = "api.googleads.loginCustomerId"

  def apply(): AdsProperties = new AdsProperties()

  def apply(config: Configuration): AdsProperties = {
    val tempProps = apply()
    tempProps.setProperty(ClientIdPath, config.get(ClientIdPath))
    tempProps.setProperty(DeveloperTokenPath, config.get(DeveloperTokenPath))
    tempProps.setProperty(ClientSecretPath, config.get(ClientSecretPath))
    tempProps
  }

  def apply(config: Configuration, refreshToken: String, loginCustomerId: String = null): AdsProperties = {
    val tempProps = apply(config)
    tempProps.setRefreshToken(refreshToken)
    tempProps.setLoginCustomerId(loginCustomerId)
    tempProps
  }
}

class AdsProperties extends Properties {

  import AdsProperties._

  def getClientId: String = getProperty(ClientIdPath)

  def setClientId(value: String): Unit = setProperty(ClientIdPath, value)

  def getDeveloperToken: String = getProperty(DeveloperTokenPath)

  def setDeveloperToken(value: String): Unit = setProperty(DeveloperTokenPath, value)

  def getLoginCustomerId: String = getProperty(LoginCustomerIdPath)

  def setLoginCustomerId(value: String): Unit = setProperty(LoginCustomerIdPath, value)

  def getClientSecret: String = getProperty(ClientSecretPath)

  def setClientSecret(value: String): Unit = setProperty(ClientSecretPath, value)

  def getRefreshToken: String = getProperty(AdsProperties.RefreshTokenPath)

  def setRefreshToken(value: String): Unit = setProperty(AdsProperties.RefreshTokenPath, value)
}
