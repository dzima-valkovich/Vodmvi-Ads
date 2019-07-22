package utils.google

import java.io.File

import com.google.ads.googleads.lib.GoogleAdsClient

object AdsClientFactory {
  //  def apply(): GoogleAdsClient = GoogleAdsClient
  //    .newBuilder()
  //    .fromPropertiesFile()
  //    .build()
  //
  //  def apply(propertyFile: File): GoogleAdsClient = GoogleAdsClient
  //    .newBuilder()
  //    .fromPropertiesFile(propertyFile)
  //    .build()

  def google: GoogleAdsClient = GoogleAdsClient
    .newBuilder()
    .fromPropertiesFile()
    .build()

  def google(propertyFile: File): GoogleAdsClient = GoogleAdsClient
    .newBuilder()
    .fromPropertiesFile(propertyFile)
    .build()
}
