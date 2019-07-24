package actors.google.workers

import actors.google.workers.ExpandedTextAdsActor.{AddExpandedTextsRequest, AddExpandedTextsResponse}
import akka.actor.{Actor, Props}
import com.google.ads.googleads.v2.services.AdGroupAdOperation
import model.PriceListRecord
import model.ads.{Ad, AdGroup, Customer}
import utils.google.AdsClientFactory
import actors.google.implicits.AdRich

import scala.collection.JavaConverters._

object ExpandedTextAdsActor {

  def props: Props = Props[ExpandedTextAdsActor]

  final case class AddExpandedTextsRequest(customer: Customer, ads: Iterable[Ad])

  final case class AddExpandedTextsResponse(ads: Iterable[Ad])

  trait Factory {
    def apply(): Actor
  }

}

class ExpandedTextAdsActor extends Actor {
  private def addExpandedTexts(customer: Customer, ads: Iterable[Ad]): Iterable[Ad] = {
    val operations = ads
      .map(ad => AdGroupAdOperation.newBuilder().setCreate(ad.toGoogle).build())
      .toList
      .asJava
    val client = AdsClientFactory.google.getLatestVersion.createAdGroupAdServiceClient
    val response = client.mutateAdGroupAds(customer.id.get, operations)
    client.shutdown()
    ads.zip(response.getResultsList.asScala)
      .map(t => t._1.copy(id = Some(t._2.getResourceName.split('/')(3))))
  }

  override def receive: Receive = {
    case AddExpandedTextsRequest(customer, ads) =>
      val response = addExpandedTexts(customer, ads)
      sender() ! AddExpandedTextsResponse(response)
  }
}
