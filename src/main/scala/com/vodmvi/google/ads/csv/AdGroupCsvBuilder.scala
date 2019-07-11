package com.vodmvi.google.ads.csv

import java.io.{File, Reader}

import com.google.ads.googleads.v2.enums.AdGroupTypeEnum.AdGroupType
import com.google.ads.googleads.v2.enums.AdGroupStatusEnum.AdGroupStatus
import com.google.ads.googleads.v2.resources.AdGroup
import com.google.ads.googleads.v2.utils.ResourceNames
import com.google.protobuf.{Int64Value, StringValue}

import scala.io.Source

object ReadAdGroupFromCsv {
  def apply(csvFile: File): Iterator[AdGroup] = {
    val csvReader = Source.fromFile(csvFile)
    val adGroupStringData = csvReader.getLines().map(_.split(','))
    AdGroupStatus.PAUSED
    AdGroupType.DISPLAY_STANDARD

    val campaingResourceName = ResourceNames.campaign(8705483891L, 2060569122L)

    adGroupStringData.map(adGroupRecord => AdGroup.newBuilder()
      .setName(StringValue.of(adGroupRecord(0)))
      .setStatus(AdGroupStatus.valueOf(adGroupRecord(1)))
      .setCampaign(StringValue.of(campaingResourceName))
      .setType(AdGroupType.valueOf(adGroupRecord(2)))
      .setCpcBidMicros(Int64Value.of(adGroupRecord(3).toLong))
      .build())
  }

  def buildAdGroupsFromCsv(csvFile: File)(customerId: Long, campaingId: Long) = {
    val csvReader = Source.fromFile(csvFile)
    val adGroupStringData = csvReader.getLines().map(_.split(','))

    val campaingResourceName = ResourceNames.campaign(customerId, campaingId)

    adGroupStringData.map(adGroupRecord => AdGroup.newBuilder()
      .setName(StringValue.of(adGroupRecord(0)))
      .setStatus(AdGroupStatus.valueOf(adGroupRecord(1)))
      .setCampaign(StringValue.of(campaingResourceName))
      .setType(AdGroupType.valueOf(adGroupRecord(2)))
      .setCpcBidMicros(Int64Value.of(adGroupRecord(3).toLong))
      .build())
  }
}
