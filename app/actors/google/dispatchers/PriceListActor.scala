package actors.google.dispatchers

import java.io.File
import java.util.Date

import actors.concurrent.Synchronizer
import actors.google.dispatchers.PriceListActor.{ProcessPriceListRequest, ProcessPriceListResponse}
import actors.google.workers.AdGroupActor.{AddAdGroupsRequest, AddAdGroupsResponse}
import actors.google.workers.CampaignActor.{AddCampaignRequest, AddCampaignResponse}
import actors.google.workers.CampaignBudgetActor.{AddCampaignBudgetRequest, AddCampaignBudgetResponse}
import actors.google.workers.ExpandedTextAdsActor.{AddExpandedTextsRequest, AddExpandedTextsResponse}
import actors.google.workers.KeywordActor.{AddKeywordsRequest, AddKeywordsResponse}
import actors.google.workers._
import actors.workers.PriceListFileActor
import actors.workers.PriceListFileActor.{ReadFromCsvRequest, ReadFromCsvResponse}
import akka.actor.{Actor, ActorRef}
import com.google.inject.assistedinject.Assisted
import javax.inject.Inject
import model.PriceListRecord
import model.ads._
import play.api.libs.concurrent.InjectedActorSupport

object PriceListActor {

  final case class ProcessPriceListRequest(priceListFile: File)

  final case class ProcessPriceListResponse()

  trait Factory {
    def apply(customer: Customer): Actor
  }

}

class PriceListActor @Inject()(
                                val priceListFileActorFactory: PriceListFileActor.Factory,
                                val campaignBudgetActorFactory: CampaignBudgetActor.Factory,
                                val campaignActorFactory: CampaignActor.Factory,
                                val adGroupActorFactory: AdGroupActor.Factory,
                                val expandedTextAdsActorFactory: ExpandedTextAdsActor.Factory,
                                val keywordActorFactory: KeywordActor.Factory,
                                @Assisted val customer: Customer
                              )
  extends Actor with InjectedActorSupport {


  private lazy val adGroupActorRef = injectedChild(adGroupActorFactory(), "AdGroupActor")

  private lazy val campaignBudgetActorRef = injectedChild(campaignBudgetActorFactory(), "CampaignBudgetActor")

  private lazy val priceListFileActorRef = injectedChild(priceListFileActorFactory(), "PriceListFileActor")

  private lazy val actorRef = injectedChild(campaignActorFactory(), "CampaignActor")

  private lazy val expandedTextAdsActorRef = injectedChild(expandedTextAdsActorFactory(), "ExpandedTextAdsActor")

  private lazy val keywordActorRef = injectedChild(keywordActorFactory(), "KeywordActor")

  private val readFromCsvAndAddCampaignSynchronizer = Synchronizer[Iterable[PriceListRecord], Campaign](
    (objectA: Iterable[PriceListRecord], objectB: Campaign) => {
      val adGroups: Iterable[AdGroup] = createAdGroups(objectA, objectB)
      adGroupActorRef ! AddAdGroupsRequest(customer, adGroups)
    }
  )

  private var firstSender: ActorRef = _

  //A list of tuples of price list records and group's resource names
  private var tuples: Iterable[(PriceListRecord, AdGroup)] = _

  private def createAdGroups(priceList: Iterable[PriceListRecord], campaign: Campaign): Iterable[AdGroup] =
    priceList.map(pl => AdGroup(customer, campaign, s"${campaign.id} ${pl.keyword} (${new Date()}"))


  private def createAds(tuples: Iterable[(PriceListRecord, AdGroup)]): Iterable[Ad] =
    tuples.map(t => Ad(customer, t._2, "Offer you " + t._1.keyword,
      s"The best cost (${t._1.price})", "We have all that you need", "http://www.example.com"))

  private def createKeywords(tuples: Iterable[(PriceListRecord, AdGroup)]): Iterable[Keyword] =
    tuples.map(t => Keyword(customer, t._2, t._1.keyword))

  override def receive: Receive = {
    case ProcessPriceListRequest(priceListFile: File) =>
      firstSender = sender()
      val campaignBudget = CampaignBudget(customer, s"Test budget2! (${new Date()})", 500000)
      campaignBudgetActorRef ! AddCampaignBudgetRequest(campaignBudget)
      priceListFileActorRef ! ReadFromCsvRequest(priceListFile)

    case AddCampaignBudgetResponse(mutateBudget) =>
      val campaign = Campaign(customer, mutateBudget, s"Test campaign! (${new Date()})", new Date(), 30)
      actorRef ! AddCampaignRequest(campaign)

    case AddCampaignResponse(mutateCampaign) => readFromCsvAndAddCampaignSynchronizer.synchronizeObjectB(mutateCampaign)

    case ReadFromCsvResponse(priceList: Iterable[PriceListRecord]) => readFromCsvAndAddCampaignSynchronizer.synchronizeObjectA(priceList)

    case AddAdGroupsResponse(adGroups) =>
      tuples = readFromCsvAndAddCampaignSynchronizer.objectA
        .zip(adGroups)
      val ads = createAds(tuples)
      expandedTextAdsActorRef ! AddExpandedTextsRequest(customer, ads)

    case AddExpandedTextsResponse(_) =>
      val keywords = createKeywords(tuples)
      keywordActorRef ! AddKeywordsRequest(customer, keywords)

    case AddKeywordsResponse(_) =>
      firstSender ! "Answer"
  }
}
