package aktors.google

import akka.actor.{Actor, Props}
import aktors.google.CampaignActor.{AddCampaignAndBudgetRequest, AddCampaignRequest}
import aktors.google.PriceListActor.AddCampaignResponse
import com.google.ads.googleads.v2.common.ManualCpc
import com.google.ads.googleads.v2.enums.AdvertisingChannelTypeEnum.AdvertisingChannelType
import com.google.ads.googleads.v2.enums.CampaignStatusEnum.CampaignStatus
import com.google.ads.googleads.v2.resources.Campaign
import com.google.ads.googleads.v2.resources.Campaign.NetworkSettings
import com.google.ads.googleads.v2.services.{CampaignOperation, MutateCampaignsResponse}
import com.google.common.collect.ImmutableList
import com.google.protobuf.{BoolValue, StringValue}
import org.joda.time.DateTime
import utils.google.AdsClientFactory

object CampaignActor {
  def addCampaign(customerId: Long, campaignName: String, budgetResourceName: String): MutateCampaignsResponse = {
    val networkSettings: NetworkSettings =
      NetworkSettings.newBuilder()
        .setTargetGoogleSearch(BoolValue.of(true))
        .setTargetSearchNetwork(BoolValue.of(true))
        .setTargetContentNetwork(BoolValue.of(false))
        .setTargetPartnerSearchNetwork(BoolValue.of(false))
        .build()

    val campaign: Campaign =
      Campaign.newBuilder()
        .setName(StringValue.of(campaignName))
        .setAdvertisingChannelType(AdvertisingChannelType.SEARCH)
        .setStatus(CampaignStatus.ENABLED)
        .setManualCpc(ManualCpc.newBuilder().build())
        .setCampaignBudget(StringValue.of(budgetResourceName))
        .setNetworkSettings(networkSettings)
        .setStartDate(StringValue.of(new DateTime().plusDays(1).toString("yyyyMMdd")))
        .setEndDate(StringValue.of(new DateTime().plusDays(30).toString("yyyyMMdd")))
        .build()

    val op = CampaignOperation.newBuilder().setCreate(campaign).build()
    val client = AdsClientFactory.google.getLatestVersion.createCampaignServiceClient()
    val response = client.mutateCampaigns(customerId.toString, ImmutableList.of(op))
    client.shutdown()
    response
  }

  def props: Props = Props[CampaignActor]

  final case class AddCampaignRequest(customerId: Long, campaignName: String, budgetResourceName: String)

  final case class AddCampaignAndBudgetRequest(customerId: Long, campaignName: String, budgetName: String, amount: Long)

}

class CampaignActor extends Actor {

  override def receive: Receive = {
    case AddCampaignRequest(customerId: Long, campaignName: String, budgetResourceName: String) =>
      val response = CampaignActor.addCampaign(customerId, campaignName, budgetResourceName)
      sender() ! AddCampaignResponse(response)

    case AddCampaignAndBudgetRequest(customerId: Long, campaignName: String, budgetName: String, amount: Long) =>
      val budgetResponse = CampaignBudgetActor.addCampaignBudget(customerId, budgetName, amount)
      val campaignResponse = CampaignActor.addCampaign(customerId, campaignName, budgetResponse.getResults(0).getResourceName)
      sender() ! AddCampaignResponse(campaignResponse)
  }

}
