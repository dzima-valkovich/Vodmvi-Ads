package aktors.google

import akka.actor.Actor
import aktors.PriceListActor.Report
import com.google.ads.googleads.v2.common.KeywordInfo
import com.google.ads.googleads.v2.enums.AdGroupCriterionStatusEnum.AdGroupCriterionStatus
import com.google.ads.googleads.v2.enums.KeywordMatchTypeEnum.KeywordMatchType
import com.google.ads.googleads.v2.resources.AdGroupCriterion
import com.google.ads.googleads.v2.services.AdGroupCriterionOperation
import com.google.protobuf.StringValue
import model.PriceListRecord
import utils.google.AdsClientFactory

object KeywordActor {

  final case class AddKeyWord(customerId: Long, tuple: Iterator[(PriceListRecord, String)])

}

class KeywordActor extends Actor {

  import aktors.google.KeywordActor.AddKeyWord
  import collection.JavaConverters._

  override def receive: Receive = {
    case AddKeyWord(customerId, tuple) =>
      val criterionOperations = tuple
        .map(t => {
          val keywordInfo = KeywordInfo.newBuilder()
            .setText(StringValue.of(t._1.keyword))
            .setMatchType(KeywordMatchType.EXACT)
            .build()

          AdGroupCriterion.newBuilder()
            .setAdGroup(StringValue.of(t._2))
            .setStatus(AdGroupCriterionStatus.ENABLED)
            .setKeyword(keywordInfo)
            .build()
        })
        .map(AdGroupCriterionOperation.newBuilder().setCreate(_).build())
        .toList
        .asJava

      val client = AdsClientFactory.google.getLatestVersion.createAdGroupCriterionServiceClient()
      val response = client.mutateAdGroupCriteria(customerId.toString, criterionOperations)
      client.shutdown()
      sender ! Report(("keywordResponse", response), 1)
  }
}
