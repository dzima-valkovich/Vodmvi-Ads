package actors.google.workers

import actors.google.workers.CampaignActor.{AddCampaignRequest, AddCampaignResponse}
import akka.actor.{Actor, Props}
import com.google.ads.googleads.v2.services.CampaignOperation
import com.google.common.collect.ImmutableList
import model.ads.Campaign
import utils.google.AdsClientFactory
import actors.google.implicits.CampaignRich
import actors.google.implicits.AdsClientFactoryRich
import javax.inject.Inject

object CampaignActor {

  def props: Props = Props[CampaignActor]

  final case class AddCampaignRequest(campaign: Campaign)

  final case class AddCampaignResponse(campaign: Campaign)

  trait Factory {
    def apply(): Actor
  }

}

class CampaignActor @Inject() (adsClientFactory: AdsClientFactory) extends Actor {
  private def addCampaign(campaign: Campaign): Campaign = {
    val op = CampaignOperation.newBuilder().setCreate(campaign.toGoogle).build()
    val client = adsClientFactory.google(campaign.customer).getLatestVersion.createCampaignServiceClient()
    val response = client.mutateCampaigns(campaign.customer.id.get, ImmutableList.of(op))
    client.shutdown()
    campaign.copy(id = Some(response.getResults(0).getResourceName.split('/')(3)))
  }

  override def receive: Receive = {
    case AddCampaignRequest(campaign: Campaign) =>
      val response = addCampaign(campaign)
      sender() ! AddCampaignResponse(response)
  }
}
