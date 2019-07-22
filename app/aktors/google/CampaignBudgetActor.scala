package aktors.google

import akka.actor.{Actor, Props}
import aktors.google.CampaignBudgetActor.AddCampaignBudgetRequest
import aktors.google.PriceListActor.AddCampaignBudgetResponse
import com.google.ads.googleads.v2.enums.BudgetDeliveryMethodEnum.BudgetDeliveryMethod
import com.google.ads.googleads.v2.resources.CampaignBudget
import com.google.ads.googleads.v2.services.{CampaignBudgetOperation, MutateCampaignBudgetsResponse}
import com.google.common.collect.ImmutableList
import com.google.protobuf.{Int64Value, StringValue}
import utils.google.AdsClientFactory

object CampaignBudgetActor {
  def addCampaignBudget(customerId: Long, name: String, amount: Long): MutateCampaignBudgetsResponse = {
    val budget = CampaignBudget.newBuilder()
      .setName(StringValue.of(name))
      .setDeliveryMethod(BudgetDeliveryMethod.STANDARD)
      .setAmountMicros(Int64Value.of(amount))
      .build()

    val op = CampaignBudgetOperation.newBuilder().setCreate(budget).build()
    val client = AdsClientFactory.google.getLatestVersion.createCampaignBudgetServiceClient()
    val response = client.mutateCampaignBudgets(customerId.toString, ImmutableList.of(op))
    client.shutdown()
    response
  }

  def props: Props = Props[CampaignBudgetActor]

  final case class AddCampaignBudgetRequest(customerId: Long, name: String, amount: Long)

  //  final case class AddCampaignBudget(customerId: Long, name: String)

  //  final case class AddCampaignBudgetAndCampaign(customerId: Long, budgetName: String, compaignName: String)

}

class CampaignBudgetActor extends Actor {

  override def receive: Receive = {
    case AddCampaignBudgetRequest(customerId: Long, name: String, amount: Long) =>
      val response = CampaignBudgetActor.addCampaignBudget(customerId: Long, name: String, amount: Long)
      sender ! AddCampaignBudgetResponse(response)
  }
}
