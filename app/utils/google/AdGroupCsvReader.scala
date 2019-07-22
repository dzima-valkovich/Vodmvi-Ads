package utils.google

import java.io.File
import java.util.Date

import com.google.ads.googleads.v2.enums.AdGroupStatusEnum.AdGroupStatus
import com.google.ads.googleads.v2.enums.AdGroupTypeEnum.AdGroupType
import com.google.ads.googleads.v2.resources.AdGroup
import com.google.ads.googleads.v2.utils.ResourceNames
import com.google.protobuf.{Int64Value, StringValue}

import scala.io.Source

object AdGroupCsvReader {

  def apply(csvFile: File, withDate: Boolean = true, customerId: Long): Iterator[AdGroup.Builder] = readAll(csvFile, withDate, customerId)

  def readAll(csvFile: File, withDate: Boolean, customerId: Long): Iterator[AdGroup.Builder] = {
    val csvSource = Source.fromFile(csvFile)
    val adGroupStringData = csvSource.getLines().map(_.split(','))

    adGroupStringData.map(adGroupRecord => AdGroup.newBuilder()
      .setName(
        if (withDate)
          StringValue.of(adGroupRecord(1) + ' ' + '(' + new Date() + ')')
        else
          StringValue.of(adGroupRecord(1))
      )
      .setStatus(AdGroupStatus.valueOf(adGroupRecord(2)))
      .setCampaign(StringValue.of(ResourceNames.campaign(customerId, adGroupRecord(0).toLong)))
      .setType(AdGroupType.valueOf(adGroupRecord(3)))
      .setCpcBidMicros(Int64Value.of(adGroupRecord(4).toLong)))
  }

  def readAll(csvFile: File, withDate: Boolean)(customerId: Long, campaignId: Long): Iterator[AdGroup.Builder] = {
    val csvSource = Source.fromFile(csvFile)
    val adGroupStringData = csvSource.getLines().map(_.split(','))

    adGroupStringData.map(adGroupRecord => AdGroup.newBuilder()
      .setName(
        if (withDate)
          StringValue.of(adGroupRecord(0) + ' ' + '(' + new Date() + ')')
        else
          StringValue.of(adGroupRecord(0))
      )
      .setStatus(AdGroupStatus.valueOf(adGroupRecord(1)))
      .setCampaign(StringValue.of(ResourceNames.campaign(customerId, campaignId)))
      .setType(AdGroupType.valueOf(adGroupRecord(2)))
      .setCpcBidMicros(Int64Value.of(adGroupRecord(3).toLong)))
  }

  def readAllAndBuild(csvFile: File, withDate: Boolean = true)(customerId: Long): Iterator[AdGroup] =
    readAll(csvFile, withDate, customerId).map(_.build())

}
