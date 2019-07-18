package aktors.google

import akka.actor.Actor
import aktors.google.CampaignBudgetActor.AddBudget
import com.google.ads.googleads.v2.enums.BudgetDeliveryMethodEnum.BudgetDeliveryMethod
import com.google.ads.googleads.v2.resources.CampaignBudget
import com.google.ads.googleads.v2.services.CampaignBudgetOperation
import com.google.common.collect.ImmutableList
import com.google.protobuf.{Int64Value, StringValue}
import utils.google.AdsClientFactory

object CampaignBudgetActor {

  final case class AddCampaignBudget(customerId: Long, name: String)

}

class CampaignBudgetActor extends Actor {

  import aktors.google.CampaignBudgetActor.AddCampaignBudget

  override def receive: Receive = {
    case AddCampaignBudget(customerId, name) =>
      val client = AdsClientFactory.google.getLatestVersion.createCampaignBudgetServiceClient()

      val budget = CampaignBudget.newBuilder()
        .setName(StringValue.of(name))
        .setDeliveryMethod(BudgetDeliveryMethod.STANDARD)
        .setAmountMicros(Int64Value.of(500000))
        .build()

      val op = CampaignBudgetOperation.newBuilder().setCreate(budget).build()
      val response = client.mutateCampaignBudgets(customerId.toString, ImmutableList.of(op))

  }
}
