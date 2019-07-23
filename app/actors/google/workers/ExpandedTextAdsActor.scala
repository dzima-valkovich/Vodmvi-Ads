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

  final case class AddExpandedTextsRequest(customer: Customer, tuples: Iterable[(PriceListRecord, AdGroup)])

  final case class AddExpandedTextsResponse(ads: Iterable[Ad])

  trait Factory {
    def apply(): Actor
  }

}

class ExpandedTextAdsActor extends Actor {
  private def addExpandedTexts(customer: Customer, tuples: Iterable[(PriceListRecord, AdGroup)]): Iterable[Ad] = {
    val ads = tuples.map(t => Ad(customer, t._2, "Offer you " + t._1.keyword,
      "The best cost " + '(' + t._1.price + ')', "We have all that you need", "http://www.example.com"))

    val operations = ads
      .map(ad => AdGroupAdOperation.newBuilder().setCreate(ad.toGoogle).build())
      .toList
      .asJava
    val client = AdsClientFactory.google.getLatestVersion.createAdGroupAdServiceClient
    val response = client.mutateAdGroupAds(customer.id.toString, operations)
    client.shutdown()
    ads.zip(response.getResultsList.asScala)
      .map(t => {
        t._1.id = t._2.getResourceName.split('/')(3)
        t._1
      })
  }

  override def receive: Receive = {
    case AddExpandedTextsRequest(customer, tuples) =>
      val response = addExpandedTexts(customer, tuples)
      sender() ! AddExpandedTextsResponse(response)
  }
}
