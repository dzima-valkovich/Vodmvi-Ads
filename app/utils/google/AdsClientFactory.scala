package utils.google

import java.io.File

import com.google.ads.googleads.lib.GoogleAdsClient
import com.typesafe.config.ConfigException
import javax.inject.Inject
import play.api.Configuration

class AdsClientFactory @Inject()(config: Configuration) {

  private lazy val props = AdsProperties(config)

  def google(refreshToken: String, loginCustomerId: String = null): GoogleAdsClient =
    try {
      if (loginCustomerId != null) {
        props.setLoginCustomerId(loginCustomerId)
      }
      props.setRefreshToken(refreshToken)

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
