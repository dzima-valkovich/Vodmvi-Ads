package aktors.google

import akka.actor.{Actor, Props}
import aktors.PriceListActor.Report
import com.google.ads.googleads.v2.common.ExpandedTextAdInfo
import com.google.ads.googleads.v2.enums.AdGroupAdStatusEnum.AdGroupAdStatus
import com.google.ads.googleads.v2.resources.{Ad, AdGroupAd}
import com.google.ads.googleads.v2.services.AdGroupAdOperation
import com.google.protobuf.StringValue
import model.PriceListRecord
import utils.google.AdsClientFactory

object ExpandedTextAdsActor {
  def props: Props = Props[ExpandedTextAdsActor]

  final case class AddExpandedTexts(customerId: Long, tuple: Iterator[(PriceListRecord, String)])

}

class ExpandedTextAdsActor extends Actor {

  import aktors.google.ExpandedTextAdsActor.AddExpandedTexts
  import collection.JavaConverters._

  override def receive: Receive = {
    case AddExpandedTexts(customerId, tuple) =>
      val operations = tuple
        .map(t => {
          val exp = ExpandedTextAdInfo
            .newBuilder()
            .setHeadlinePart1(StringValue.of("Shop of " + t._1.keyword))
            .setHeadlinePart2(StringValue.of("The best cost in the town " + '(' + t._1.price + ')'))
            .setDescription(StringValue.of("We have all that you need!!!"))
            .build()

          val ad = Ad.newBuilder()
            .setExpandedTextAd(exp)
            .addFinalUrls(StringValue.of("www.example.com"))
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
      sender ! Report(("expandedTextAdsResponse", response), 1)
  }

}
