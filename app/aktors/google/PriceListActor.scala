package aktors.google

import java.io.File
import java.util.Date

import akka.actor.{Actor, ActorRef, Props}
import aktors.PriceListFileActor
import aktors.PriceListFileActor.{ReadFromCsvRequest, ReadFromCsvResponse}
import aktors.google.AdGroupActor.AddAdGroupsRequest
import aktors.google.CampaignActor.AddCampaignRequest
import aktors.google.CampaignBudgetActor.AddCampaignBudgetRequest
import aktors.google.ExpandedTextAdsActor.AddExpandedTextsRequest
import aktors.google.KeywordActor.AddKeywordsRequest
import aktors.google.PriceListActor.{AddAdGroupsResponse, AddCampaignBudgetResponse, AddCampaignResponse, AddExpandedTextsResponse, AddKeywordsResponse, ProcessPriceListRequest, Synchronizer}
import com.google.ads.googleads.v2.services.{MutateAdGroupAdsResponse, MutateAdGroupCriteriaResponse, MutateAdGroupsResponse, MutateCampaignBudgetsResponse, MutateCampaignsResponse}
import model.PriceListRecord

import collection.JavaConverters._

object PriceListActor {

  final case class Synchronizer[X, Y](userFunction: (X, Y) => Unit) {
    private var count: Int = 0
    private var _objectA: X = _
    private var _objectB: Y = _

    def objectA_=(o: X): Unit = {
      count += 1
      _objectA = o
      foo()
    }

    def objectA: X = _objectA

    def objectB_=(o: Y): Unit = {
      count += 1
      _objectB = o
      foo()
    }

    def objectB: Y = _objectB

    private def foo(): Unit = {
      if (count == 2) {
        userFunction(_objectA, _objectB)
      }
    }
  }

  def props(customerId: Long): Props = Props(new PriceListActor(customerId))

  final case class ProcessPriceListRequest(priceList: File)

  final case class AddCampaignBudgetResponse(response: MutateCampaignBudgetsResponse)

  final case class AddCampaignResponse(response: MutateCampaignsResponse)

  final case class AddAdGroupsResponse(response: MutateAdGroupsResponse)

  final case class AddExpandedTextsResponse(response: MutateAdGroupAdsResponse)

  final case class AddKeywordsResponse(response: MutateAdGroupCriteriaResponse)

}

class PriceListActor(val customerId: Long) extends Actor {

  private val ReadFromCsvAndAddCampaignSynchronizer = Synchronizer[Iterable[PriceListRecord], MutateCampaignsResponse](
    (objectA: Iterable[PriceListRecord], objectB: MutateCampaignsResponse) => {
      val adGroupActorRef = context.actorOf(AdGroupActor.props)
      val campaignId = objectB.getResults(0).getResourceName.split('/')(3).toLong
      adGroupActorRef ! AddAdGroupsRequest(customerId, campaignId, objectA)
    }
  )

  private var firstSender: ActorRef = _

  //A list of tuples of price list records and group's resource names
  private var tuple: Iterable[(PriceListRecord, String)] = _

  override def receive: Receive = {
    case ProcessPriceListRequest(priceListFile: File) =>
      firstSender = sender()
      val campaignBudgetActorRef = context.actorOf(CampaignBudgetActor.props)
      campaignBudgetActorRef ! AddCampaignBudgetRequest(customerId, "Test budget! " + ' ' + '(' + new Date() + ')', 500000)

      val priceListFileActorRef = context.actorOf(PriceListFileActor.props)
      priceListFileActorRef ! ReadFromCsvRequest(priceListFile)

    case AddCampaignBudgetResponse(response: MutateCampaignBudgetsResponse) =>
      val actorRef = context.actorOf(CampaignActor.props)
      actorRef ! AddCampaignRequest(customerId, "Test campaign! " + ' ' + '(' + new Date() + ')', response.getResults(0).getResourceName)

    case AddCampaignResponse(response: MutateCampaignsResponse) => ReadFromCsvAndAddCampaignSynchronizer.objectB = response

    case ReadFromCsvResponse(priceList: Iterable[PriceListRecord]) => ReadFromCsvAndAddCampaignSynchronizer.objectA = priceList

    case AddAdGroupsResponse(response: MutateAdGroupsResponse) =>
      tuple = ReadFromCsvAndAddCampaignSynchronizer.objectA
        .zip(response.getResultsList.asScala.map(_.getResourceName))

      val expandedTextAdsActorRef = context.actorOf(ExpandedTextAdsActor.props)
      expandedTextAdsActorRef ! AddExpandedTextsRequest(customerId, tuple)

    case AddExpandedTextsResponse(_: MutateAdGroupAdsResponse) =>
      val keywordActorRef = context.actorOf(KeywordActor.props)
      keywordActorRef ! AddKeywordsRequest(customerId, tuple)

    case AddKeywordsResponse(_: MutateAdGroupCriteriaResponse) =>
      firstSender ! "Csv has been processed"
  }
}
