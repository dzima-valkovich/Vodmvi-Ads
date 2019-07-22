package utils.google

import java.io.File
import java.util.Date

import com.google.ads.googleads.v2.common.{ExpandedTextAdInfo, KeywordInfo}
import com.google.ads.googleads.v2.enums.AdGroupAdStatusEnum.AdGroupAdStatus
import com.google.ads.googleads.v2.enums.AdGroupCriterionStatusEnum.AdGroupCriterionStatus
import com.google.ads.googleads.v2.enums.AdGroupStatusEnum.AdGroupStatus
import com.google.ads.googleads.v2.enums.AdGroupTypeEnum.AdGroupType
import com.google.ads.googleads.v2.enums.KeywordMatchTypeEnum.KeywordMatchType
import com.google.ads.googleads.v2.resources.{Ad, AdGroup, AdGroupAd, AdGroupCriterion}
import com.google.ads.googleads.v2.utils.ResourceNames
import com.google.protobuf.{Int64Value, StringValue}

import scala.io.Source

object implicits {

  implicit class AdGroupBuilderIterator(val iterator: Iterator[AdGroup.Builder]) {

    def build: Iterator[AdGroup] = iterator.map(_.build())
  }

  implicit class AdGroupBuilderRich(val builder: AdGroup.Builder) {

    def fromCsv(csvFile: File, withDate: Boolean)(customerId: Long): Iterator[AdGroup.Builder] =
      AdGroupCsvReader.readAll(csvFile, withDate, customerId)

    def fromArray(arr: Array[String], withDate: Boolean)(customerId: Long, campaignId: Long): AdGroup.Builder =
      builder
        .setName(
          if (withDate)
            StringValue.of(arr(0) + ' ' + '(' + new Date() + ')')
          else
            StringValue.of(arr(0))
        )
        .setStatus(AdGroupStatus.valueOf(arr(1)))
        .setCampaign(StringValue.of(ResourceNames.campaign(customerId, campaignId)))
        .setType(AdGroupType.valueOf(arr(2)))
        .setCpcBidMicros(Int64Value.of(arr(3).toLong))

  }

  implicit class ExpandedTextAdInfoBuilderRich(val builder: ExpandedTextAdInfo.Builder) {

    def fromArray(arr: Array[String]): ExpandedTextAdInfo.Builder =
      builder
        .setHeadlinePart1(StringValue.of(arr(0)))
        .setHeadlinePart2(StringValue.of(arr(1)))
        //        .setHeadlinePart2(StringValue.of(arr(2)))
        .setDescription(StringValue.of(arr(2)))

    //        .setDescription2(StringValue.of(arr(4)))

  }

  implicit class AdBuilderRich(val builder: Ad.Builder) {

    def fromArray(arr: Array[String], expandedText: ExpandedTextAdInfo): Ad.Builder =
      builder
        .setExpandedTextAd(expandedText)
        .addFinalUrls(StringValue.of(arr(0)))

  }

  implicit class AdGroupAdBuilderRich(val builder: AdGroupAd.Builder) {

    def fromArray(arr: Array[String], ad: Ad)(clientId: Long, groupId: Long): AdGroupAd.Builder = {
      val adGroupResourceName: String = ResourceNames.adGroup(clientId, groupId)

      builder
        .setAdGroup(StringValue.of(adGroupResourceName))
        .setStatus(AdGroupAdStatus.valueOf(arr(0)))
        .setAd(ad)
    }

  }

  implicit class KeywordInfoBuilderRich(val builder: KeywordInfo.Builder) {

    def fromArray(arr: Array[String]): KeywordInfo.Builder =
      builder
        .setText(StringValue.of(arr(0)))
        .setMatchType(KeywordMatchType.valueOf(arr(1)))

  }

  implicit class AdGroupCriterionRich(val builder: AdGroupCriterion.Builder) {

    def fromArray(arr: Array[String], keywordInfo: KeywordInfo)(clientId: Long, groupId: Long): AdGroupCriterion.Builder = {
      val adGroupResourceName: String = ResourceNames.adGroup(clientId, groupId)

      builder
        .setAdGroup(StringValue.of(adGroupResourceName))
        .setStatus(AdGroupCriterionStatus.valueOf(arr(0)))
        .setKeyword(keywordInfo)
    }

  }

  //  implicit def userToClient(user: User): Client = user.asInstanceOf[Client]


  //  implicit def toAdGroupRich(adGroup: AdGroup): AdGroupRich = AdGroupRich(adGroup)
}
