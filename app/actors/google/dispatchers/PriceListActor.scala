package actors.google.dispatchers

import java.io.File
import java.util.Date

import actors.concurrent.Synchronizer
import actors.google.dispatchers.PriceListActor.ProcessPriceListRequest
import actors.google.workers.AdGroupActor.{AddAdGroupsRequest, AddAdGroupsResponse}
import actors.google.workers.CampaignActor.{AddCampaignRequest, AddCampaignResponse}
import actors.google.workers.CampaignBudgetActor.{AddCampaignBudgetRequest, AddCampaignBudgetResponse}
import actors.google.workers.ExpandedTextAdsActor.{AddExpandedTextsRequest, AddExpandedTextsResponse}
import actors.google.workers.KeywordActor.{AddKeywordsRequest, AddKeywordsResponse}
import actors.google.workers._
import actors.workers.PriceListFileActor
import actors.workers.PriceListFileActor.{ReadFromCsvRequest, ReadFromCsvResponse}
import akka.actor.{Actor, ActorRef, Props}
import javax.inject.Inject
import model.PriceListRecord
import model.ads._
import play.api.libs.concurrent.InjectedActorSupport

object PriceListActor {

  def props(customerId: Long): Props = Props[PriceListActor]

  final case class ProcessPriceListRequest(customer: Customer, priceList: File)

}

class PriceListActor @Inject()(
                                priceListFileActorFactory: PriceListFileActor.Factory,
                                campaignBudgetActorFactory: CampaignBudgetActor.Factory,
                                campaignActorFactory: CampaignActor.Factory,
                                adGroupActorFactory: AdGroupActor.Factory,
                                expandedTextAdsActorFactory: ExpandedTextAdsActor.Factory,
                                keywordActorFactory: KeywordActor.Factory
                              )
  extends Actor with InjectedActorSupport {

  private val readFromCsvAndAddCampaignSynchronizer = Synchronizer[Iterable[PriceListRecord], Campaign](
    (objectA: Iterable[PriceListRecord], objectB: Campaign) => {
      val adGroupActorRef = injectedChild(adGroupActorFactory(), "AdGroupActor")
      adGroupActorRef ! AddAdGroupsRequest(customer, objectB, objectA)
    }
  )

  private var customer: Customer = _

  private var firstSender: ActorRef = _

  //A list of tuples of price list records and group's resource names
  private var tuples: Iterable[(PriceListRecord, AdGroup)] = _

  override def receive: Receive = {
    case ProcessPriceListRequest(customer: Customer, priceListFile: File) =>
      this.customer = customer
      firstSender = sender()
      val campaignBudgetActorRef = injectedChild(campaignBudgetActorFactory(), "CampaignBudgetActor")
      val campaignBudget = CampaignBudget(customer, "Test budget2! " + ' ' + '(' + new Date() + ')', 500000)
      campaignBudgetActorRef ! AddCampaignBudgetRequest(campaignBudget)

      val priceListFileActorRef = injectedChild(priceListFileActorFactory(), "PriceListFileActor")
      priceListFileActorRef ! ReadFromCsvRequest(priceListFile)

    case AddCampaignBudgetResponse(mutateBudget) =>
      val actorRef = injectedChild(campaignActorFactory(), "CampaignActor")
      val campaign = Campaign(customer, mutateBudget, "Test campaign! " + ' ' + '(' + new Date() + ')', new Date(), 30)
      actorRef ! AddCampaignRequest(campaign)

    case AddCampaignResponse(mutateCampaign) => readFromCsvAndAddCampaignSynchronizer.synchronizeObjectB(mutateCampaign)

    case ReadFromCsvResponse(priceList: Iterable[PriceListRecord]) => readFromCsvAndAddCampaignSynchronizer.synchronizeObjectA(priceList)

    case AddAdGroupsResponse(adGroups) =>
      tuples = readFromCsvAndAddCampaignSynchronizer.objectA
        .zip(adGroups)

      val expandedTextAdsActorRef = injectedChild(expandedTextAdsActorFactory(), "ExpandedTextAdsActor")
      expandedTextAdsActorRef ! AddExpandedTextsRequest(customer, tuples)

    case AddExpandedTextsResponse(_) =>
      val keywordActorRef = injectedChild(keywordActorFactory(), "KeywordActor")
      keywordActorRef ! AddKeywordsRequest(customer, tuples)

    case AddKeywordsResponse(_) =>
      firstSender ! "Csv has been processed"
  }
}
