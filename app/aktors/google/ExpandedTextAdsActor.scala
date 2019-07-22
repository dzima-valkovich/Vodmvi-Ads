package aktors.google

import akka.actor.{Actor, Props}
import aktors.google.ExpandedTextAdsActor.AddExpandedTextsRequest
import aktors.google.PriceListActor.AddExpandedTextsResponse
import com.google.ads.googleads.v2.common.ExpandedTextAdInfo
import com.google.ads.googleads.v2.enums.AdGroupAdStatusEnum.AdGroupAdStatus
import com.google.ads.googleads.v2.resources.{Ad, AdGroupAd}
import com.google.ads.googleads.v2.services.{AdGroupAdOperation, MutateAdGroupAdsResponse}
import com.google.protobuf.StringValue
import model.PriceListRecord
import utils.google.AdsClientFactory

import collection.JavaConverters._

object ExpandedTextAdsActor {
  def addExpandedTexts(customerId: Long, tuple: Iterable[(PriceListRecord, String)]): MutateAdGroupAdsResponse = {
    val operations = tuple
      .map(t => {
        val exp = ExpandedTextAdInfo
          .newBuilder()
          .setHeadlinePart1(StringValue.of("Offer you " + t._1.keyword))
          .setHeadlinePart2(StringValue.of("The best cost " + '(' + t._1.price + ')'))
          .setDescription(StringValue.of("We have all that you need"))
          .build()

        val ad = Ad.newBuilder()
          .setExpandedTextAd(exp)
          .addFinalUrls(StringValue.of("http://www.example.com"))
          .build()

        AdGroupAd.newBuilder()
          .setAdGroup(StringValue.of(t._2))
          .setStatus(AdGroupAdStatus.ENABLED)
          .setAd(ad)
          .build()
      })
      .map(AdGroupAdOperation.newBuilder().setCreate(_).build())
      .toList
      .asJava

    val client = AdsClientFactory.google.getLatestVersion.createAdGroupAdServiceClient
    val response = client.mutateAdGroupAds(customerId.toString, operations)
    client.shutdown()
    response
  }

  def props: Props = Props[ExpandedTextAdsActor]

  final case class AddExpandedTextsRequest(customerId: Long, tuple: Iterable[(PriceListRecord, String)])

}

class ExpandedTextAdsActor extends Actor {

  override def receive: Receive = {
    case AddExpandedTextsRequest(customerId, tuple) =>
      val response = ExpandedTextAdsActor.addExpandedTexts(customerId, tuple)
      sender() ! AddExpandedTextsResponse(response)
  }
}
