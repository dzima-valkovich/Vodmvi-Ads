package actors.google.workers

import actors.google.workers.CampaignActor.{AddCampaignRequest, AddCampaignResponse}
import akka.actor.{Actor, Props}
import com.google.ads.googleads.v2.services.CampaignOperation
import com.google.ads.googleads.v2.utils.ResourceNames
import com.google.common.collect.ImmutableList
import model.ads.Campaign
import utils.google.AdsClientFactory
import actors.google.implicits.CampaignRich

object CampaignActor {

  def props: Props = Props[CampaignActor]

  final case class AddCampaignRequest(campaign: Campaign)

  final case class AddCampaignResponse(campaign: Campaign)

  trait Factory {
    def apply(): Actor
  }

}

class CampaignActor extends Actor {
  private def addCampaign(campaign: Campaign, budgetResourceName: String): Campaign = {
    val op = CampaignOperation.newBuilder().setCreate(campaign.toGoogle).build()
    val client = AdsClientFactory.google.getLatestVersion.createCampaignServiceClient()
    val response = client.mutateCampaigns(campaign.customer.id.toString, ImmutableList.of(op))
    client.shutdown()
    campaign.id = response.getResults(0).getResourceName.split('/')(3)
    campaign
  }

  override def receive: Receive = {
    case AddCampaignRequest(campaign: Campaign) =>
      val response = addCampaign(campaign, ResourceNames.campaignBudget(campaign.customer.id.toLong, campaign.budget.id.toLong))
      sender() ! AddCampaignResponse(response)
  }
}
