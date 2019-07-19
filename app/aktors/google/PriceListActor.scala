package aktors.google

import java.io.File

import akka.actor.{Actor, Props}
import aktors.PriceListFileActor
import aktors.PriceListFileActor.{ReadFromCsvRequest, ReadFromCsvResponse}
import aktors.google.CampaignActor.AddCampaignRequest
import aktors.google.CampaignBudgetActor.AddCampaignBudgetRequest
import aktors.google.PriceListActor.{AddCampaignBudgetResponse, AddCampaignResponse, StartProcessPriceList, Synchronizer}
import com.google.ads.googleads.v2.services.{MutateCampaignBudgetsResponse, MutateCampaignsResponse}
import model.PriceListRecord
import collection.JavaConverters._

object PriceListActor {

  final case class Synchronizer[X, Y](userFunction: (X, Y) => Unit) {
    private var count: Int = 0
    private var objectA: X = _
    private var objectB: Y = _

    private def foo(): Unit = {
      if (count == 2) {
        userFunction()
      }
    }

    def synchronize(x: X): Unit = {
      count += 1
      objectA = x
      foo()
    }

    def synchronize(y: Y): Unit = {
      count += 1
      objectB = y
      foo()
    }
  }

  def props(customerId: Long): Props = Props(new PriceListActor(customerId))

  final case class StartProcessPriceList(priceList: File)

  final case class AddCampaignBudgetResponse(response: MutateCampaignBudgetsResponse)

  final case class AddCampaignResponse(response: MutateCampaignsResponse)

  /*  final case class ProcessPriceList(customerId: Long, priceList: File)

    final case class AddCampaignBudgetCommand()

    final case class AddCampaignCommand()

    final case class AddCampaignAndBudgetCommand()

    final case class AddAdGroupsCommand()

    final case class ReadPriceList(priceList: File)

    final case class Report[T](tuple: (String, Any), count: Int)*/

}

class PriceListActor(val customerId: Long) extends Actor {
  private val ReadFromCsvSynchronizerAndAddCampaign = Synchronizer[Iterator[PriceListRecord], MutateCampaignsResponse](
    (objectA: Iterator[PriceListRecord], objectB: MutateCampaignsResponse) => {
      val adGroupActorRef = context.actorOf(AdGroupActor.props)
      adGroupActorRef !
    }
  )

  override def receive: Receive = {
    case StartProcessPriceList(priceListFile: File) =>
      val campaignBudgetActorRef = context.actorOf(CampaignBudgetActor.props)
      campaignBudgetActorRef ! AddCampaignBudgetRequest(customerId, "Test budget!", 500000)
      val priceListFileActorRef = context.actorOf(PriceListFileActor.props)
      priceListFileActorRef ! ReadFromCsvRequest(priceListFile)

    case AddCampaignBudgetResponse(response: MutateCampaignBudgetsResponse) =>
      val actorRef = context.actorOf(CampaignActor.props)
      actorRef ! AddCampaignRequest(customerId, "Test campaign!", response.getResults(0).getResourceName)


    case AddCampaignResponse(response: MutateCampaignsResponse) =>

    case ReadFromCsvResponse(priceList: Iterator[PriceListRecord]) =>

  }


  //  override def receive: Receive = {
  //    case ProcessPriceList(cId, priceList) =>
  //      actionSender = sender()
  //      customerId = cId
  //      val campaignActor = context.actorOf(CampaignActor.props, "CampaignActor")
  //      campaignActor ! AddCampaignAndBudget(customerId, "Test Campaign!", "Test Budget!")
  //      self ! ReadPriceList(priceList)
  //
  //    case ReadPriceList(priceList) =>
  //      self ! Report(("priceList", CsvReader.readPriceList(priceList)), 1)
  //
  //    case Report(tuple, c) =>
  //      count += c
  //      storage += tuple
  //
  //      count match {
  //        case 3 =>
  //          val campaignId = storage("campaignResponse").asInstanceOf[MutateCampaignsResponse].getResults(0)
  //            .getResourceName.split('/')(3).toLong
  //          val records = storage("priceList").asInstanceOf[Iterator[PriceListRecord]]
  //
  //          val adGroupActor = context.actorOf(AdGroupActor.props, "AdGroupActor")
  //          adGroupActor ! AddAdGroupAndExpandedTextAdsAndKeyword(customerId, campaignId, records)
  //
  //        case 6 =>
  //          actionSender ! "Ok! All data has been created!"
  //
  //        case _ =>
  //      }
  //  }
}
