package com.vodmvi.google.ads

//import com.google.ads.googleads.lib.GoogleAdsClient
import java.io.{File, FileReader}
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import com.google.ads.googleads.lib.GoogleAdsClient
import com.google.ads.googleads.v2.services.MutateAdGroupsResponse
import com.google.ads.googleads.v2.services.{AdGroupOperation, CampaignOperation, CampaignServiceClient, MutateAdGroupsResponse}
import com.google.ads.googleads.v2.common.ManualCpc
import com.google.ads.googleads.v2.enums.AdvertisingChannelTypeEnum.AdvertisingChannelType
import com.google.ads.googleads.v2.enums.CampaignStatusEnum.CampaignStatus
import com.google.ads.googleads.v2.resources.Campaign.NetworkSettings
import com.google.ads.googleads.v2.resources.{AdGroup, Campaign, Customer}
import com.google.ads.googleads.v2.utils.ResourceNames
import com.google.api.resourcenames.ResourceName
import com.google.protobuf.{BoolValue, StringValue}
import com.vodmvi.google.ads.csv.AdGroupCsvBuilder
import com.vodmvi.google.ads.factory.GoogleAdsFactory

object Runner {
  def main(args: Array[String]): Unit = {
    val propsFile: File = new File(getClass.getClassLoader.getResource("ads.properties").toURI)
    val client = GoogleAdsFactory(propsFile)

    val a = client.getLatestVersion.createCustomerServiceClient
    val p = ResourceNames.customer(1165218958L)
    println(p)
    val s = a.getCustomer(p)
    println(s)

    //    AdGroupCsvBuilder.separator = '-'

    val adGroups = AdGroupCsvBuilder {
      new File("/home/valkovich/Desktop/company1.csv")
    }

    val operations = adGroups.map(AdGroupOperation.newBuilder().setCreate(_).build)

    import collection.JavaConverters._
    val response: MutateAdGroupsResponse = client.getLatestVersion.createAdGroupServiceClient()
      .mutateAdGroups("8705483891", operations.toList.asJava)
    println(response)
  }

}
