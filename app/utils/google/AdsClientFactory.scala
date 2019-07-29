package utils.google

import java.io.File
import java.util.Properties

import com.google.ads.googleads.lib.GoogleAdsClient
import com.typesafe.config.{ConfigException, ConfigValue}
import io.grpc.StatusRuntimeException
import javax.inject.Inject
import play.api.Configuration
import play.api.ConfigLoader.stringLoader

//object AdsClientFactory {
//
//  def apply(config: Configuration): AdsClientFactory = new AdsClientFactory(config)
//
//}

class AdsClientFactory @Inject()(config: Configuration) {

  private val ClientIdPath = "api.googleads.clientId"
  private val DeveloperTokenPath = "api.googleads.developerToken"
  private val LoginCustomerIdPath = "api.googleads.loginCustomerId"

  private val ClientSecretPath = "api.googleads.clientSecret"
  private val RefreshTokenPath = "api.googleads.refreshToken"

  private lazy val clientId = config.get(ClientIdPath)
  private lazy val developerToken = config.get(DeveloperTokenPath)
  private lazy val loginCustomerId = config.get(LoginCustomerIdPath)

  private lazy val props = {
    val tempProps = new Properties()
    tempProps.setProperty(ClientIdPath, clientId)
    tempProps.setProperty(DeveloperTokenPath, developerToken)
    tempProps.setProperty(LoginCustomerIdPath, loginCustomerId)
    tempProps
  }

  def google(clientSecret: String, refreshToken: String): GoogleAdsClient =
    try {
      props.setProperty(ClientSecretPath, clientSecret)
      props.setProperty(RefreshTokenPath, refreshToken)

      GoogleAdsClient
        .newBuilder()
        .fromProperties(props)
        .build()

    } catch {
      case _: ConfigException.Missing => google()
    }


  def google(): GoogleAdsClient = GoogleAdsClient
    .newBuilder()
    .fromPropertiesFile()
    .build()

  def google(propertyFile: File): GoogleAdsClient = GoogleAdsClient
    .newBuilder()
    .fromPropertiesFile(propertyFile)
    .build()

}
