package actors.google

import com.google.ads.googleads.v2.enums.BiddingStrategyTypeEnum.BiddingStrategyType
import com.google.ads.googleads.v2.common.{ExpandedTextAdInfo, KeywordInfo, ManualCpc}
import com.google.ads.googleads.v2.enums.AdGroupAdStatusEnum.AdGroupAdStatus
import com.google.ads.googleads.v2.enums.AdGroupCriterionStatusEnum.AdGroupCriterionStatus
import com.google.ads.googleads.v2.enums.AdGroupStatusEnum.AdGroupStatus
import com.google.ads.googleads.v2.enums.AdGroupTypeEnum.AdGroupType
import com.google.ads.googleads.v2.enums.AdvertisingChannelTypeEnum.AdvertisingChannelType
import com.google.ads.googleads.v2.enums.BudgetDeliveryMethodEnum.BudgetDeliveryMethod
import com.google.ads.googleads.v2.enums.CampaignStatusEnum.CampaignStatus
import com.google.ads.googleads.v2.enums.KeywordMatchTypeEnum.KeywordMatchType
import com.google.ads.googleads.v2.resources
import com.google.ads.googleads.v2.resources.{AdGroupCriterion, BiddingStrategy}
import com.google.ads.googleads.v2.resources.Campaign.NetworkSettings
import com.google.ads.googleads.v2.utils.ResourceNames
import com.google.protobuf.{BoolValue, Int64Value, StringValue}
import model.ads._
import org.joda.time.DateTime

object implicits {

  implicit class AdGroupRich(adGroup: AdGroup) {

    import adGroup._

    def toGoogle: resources.AdGroup = {
      import com.google.ads.googleads.v2.resources.AdGroup

      AdGroup.newBuilder()
        .setName(StringValue.of(name))
        .setStatus(AdGroupStatus.ENABLED)
        .setCampaign(StringValue.of(ResourceNames.campaign(customer.id.get.toLong, campaign.id.get.toLong)))
        .setType(AdGroupType.SEARCH_STANDARD)
//        .setCpcBidMicros(Int64Value.of(cpc))
        .build()
    }
  }

  implicit class CampaignRich(campaign: Campaign) {

    import campaign._

    def toGoogle: resources.Campaign = {
      import com.google.ads.googleads.v2.resources.Campaign

      val networkSettings: NetworkSettings =
        NetworkSettings.newBuilder()
          .setTargetGoogleSearch(BoolValue.of(true))
          .setTargetSearchNetwork(BoolValue.of(true))
          .setTargetContentNetwork(BoolValue.of(false))
          .setTargetPartnerSearchNetwork(BoolValue.of(false))
          .build()

      val startDateTime = new DateTime(startDate)

      Campaign.newBuilder()
        .setName(StringValue.of(name))
        .setAdvertisingChannelType(AdvertisingChannelType.SEARCH)
        .setStatus(CampaignStatus.ENABLED)
        .setManualCpc(ManualCpc.newBuilder().build())
        .setCampaignBudget(StringValue.of(ResourceNames.campaignBudget(customer.id.get.toLong, budget.id.get.toLong)))
        .setNetworkSettings(networkSettings)
        .setStartDate(StringValue.of(startDateTime.toString("yyyyMMdd")))
        .setEndDate(StringValue.of(startDateTime.plusDays(duration).toString("yyyyMMdd")))
        .build()
    }
  }

  implicit class CampaignBudgetRich(budget: CampaignBudget) {

    import budget._

    def toGoogle: resources.CampaignBudget = {
      import com.google.ads.googleads.v2.resources.CampaignBudget

      CampaignBudget.newBuilder()
        .setName(StringValue.of(name))
        .setDeliveryMethod(BudgetDeliveryMethod.STANDARD)
        .setAmountMicros(Int64Value.of(amount))
        .build()
    }
  }

  implicit class KeywordRich(key: Keyword) {

    import key._

    def toGoogle: AdGroupCriterion = {
      val keywordInfo = KeywordInfo.newBuilder()
        .setText(StringValue.of(keyword))
        .setMatchType(KeywordMatchType.EXACT)
        .build()

      AdGroupCriterion.newBuilder()
        .setAdGroup(StringValue.of(ResourceNames.adGroup(customer.id.get.toLong, adGroup.id.get.toLong)))
        .setStatus(AdGroupCriterionStatus.ENABLED)
        .setKeyword(keywordInfo)
        .build()
    }
  }

  implicit class AdRich(ad: Ad) {

    import ad._

    def toGoogle: resources.AdGroupAd = {
      import com.google.ads.googleads.v2.resources.{Ad, AdGroupAd}

      val exp = ExpandedTextAdInfo
        .newBuilder()
        .setHeadlinePart1(StringValue.of(headline1))
        .setHeadlinePart2(StringValue.of(headline2))
        .setDescription(StringValue.of(description))
        .build()

      val ad = Ad.newBuilder()
        .setExpandedTextAd(exp)
        .addFinalUrls(StringValue.of(url))
        .build()

      AdGroupAd.newBuilder()
        .setAdGroup(StringValue.of(ResourceNames.adGroup(customer.id.get.toLong, adGroup.id.get.toLong)))
        .setStatus(AdGroupAdStatus.ENABLED)
        .setAd(ad)
        .build()
    }
  }

}
