package aktors.google

import akka.actor.{Actor, Props}
import aktors.google.KeywordActor.AddKeywordsRequest
import aktors.google.PriceListActor.AddKeywordsResponse
import com.google.ads.googleads.v2.common.KeywordInfo
import com.google.ads.googleads.v2.enums.AdGroupCriterionStatusEnum.AdGroupCriterionStatus
import com.google.ads.googleads.v2.enums.KeywordMatchTypeEnum.KeywordMatchType
import com.google.ads.googleads.v2.resources.AdGroupCriterion
import com.google.ads.googleads.v2.services.{AdGroupCriterionOperation, MutateAdGroupCriteriaResponse}
import com.google.protobuf.StringValue
import model.PriceListRecord
import utils.google.AdsClientFactory

import collection.JavaConverters._

object KeywordActor {

  def addKeywords(customerId: Long, tuple: Iterable[(PriceListRecord, String)]): MutateAdGroupCriteriaResponse = {
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
    response
  }

  def props: Props = Props[KeywordActor]

  final case class AddKeywordsRequest(customerId: Long, tuple: Iterable[(PriceListRecord, String)])

}

class KeywordActor extends Actor {

  override def receive: Receive = {
    case AddKeywordsRequest(customerId, tuple) =>
      val response = KeywordActor.addKeywords(customerId, tuple)
      sender ! AddKeywordsResponse(response)
  }
}
