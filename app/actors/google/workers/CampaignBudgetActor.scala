package actors.google.workers

import actors.google.workers.CampaignBudgetActor.{AddCampaignBudgetRequest, AddCampaignBudgetResponse}
import akka.actor.{Actor, Props}
import com.google.ads.googleads.v2.services.CampaignBudgetOperation
import com.google.common.collect.ImmutableList
import model.ads.CampaignBudget
import utils.google.AdsClientFactory
import actors.google.implicits.CampaignBudgetRich

object CampaignBudgetActor {

  def props: Props = Props[CampaignBudgetActor]

  final case class AddCampaignBudgetRequest(campaignBudget: CampaignBudget)

  final case class AddCampaignBudgetResponse(campaignBudget: CampaignBudget)

  trait Factory {
    def apply(): Actor
  }

}

class CampaignBudgetActor extends Actor {

  private def addCampaignBudget(campaignBudget: CampaignBudget): CampaignBudget = {
    val op = CampaignBudgetOperation.newBuilder().setCreate(campaignBudget.toGoogle).build()
    val client = AdsClientFactory.google.getLatestVersion.createCampaignBudgetServiceClient()
    val response = client.mutateCampaignBudgets(campaignBudget.customer.id.toString, ImmutableList.of(op))
    client.shutdown()
    val campaignId = response.getResults(0).getResourceName.split('/')(3)
    campaignBudget.id = campaignId
    campaignBudget
  }

  override def receive: Receive = {
    case AddCampaignBudgetRequest(campaignBudget: CampaignBudget) =>
      val response = addCampaignBudget(campaignBudget)
      sender ! AddCampaignBudgetResponse(response)
  }
}
