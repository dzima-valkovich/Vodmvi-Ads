package com.vodmvi.google.ads

import java.io.File

import com.google.ads.googleads.lib.GoogleAdsClient

object VodmviAdsFabric {

  def apply(): GoogleAdsClient = GoogleAdsClient
    .newBuilder()
    .fromPropertiesFile()
    .build()


  def apply(propertyFile: File): GoogleAdsClient = GoogleAdsClient
    .newBuilder()
    .fromPropertiesFile(propertyFile)
    .build()


}
