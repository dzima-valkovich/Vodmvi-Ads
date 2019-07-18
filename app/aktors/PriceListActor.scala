package aktors

import java.io.File

import akka.actor.{Actor, Props}
import aktors.PriceListActor.ProcessPriceList
import utils.CsvReader

object PriceListActor {

  final case class ProcessPriceList(customerId: Long, priceList: File)

  final case class AddCampaign()

}

class PriceListActor extends Actor {

  import model.PriceListRecord
  import aktors.google.AdGroupActor.CreateAdGroups
  import aktors.google.CampaignBudgetActor.AddCampaignBudget

  override def receive: Receive = {
    case ProcessPriceList(customerId, priceList) =>
      val campaignBudgetActor = context.actorOf(Props.empty, "CampaignBudgetActor")
      campaignBudgetActor ! AddCampaignBudget(customerId, "Test Budget!")


      val adGroupActor = context.actorOf(Props.empty, "AdGroupActor")
      adGroupActor ! CreateAdGroups(CsvReader.readPriceList(priceList), 0, 0)

  }

}
