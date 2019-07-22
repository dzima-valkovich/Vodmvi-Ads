package aktors.google

import java.util.Date

import akka.actor.{Actor, Props}
import aktors.google.AdGroupActor.AddAdGroupsRequest
import aktors.google.PriceListActor.AddAdGroupsResponse
import com.google.ads.googleads.v2.enums.AdGroupStatusEnum.AdGroupStatus
import com.google.ads.googleads.v2.enums.AdGroupTypeEnum.AdGroupType
import com.google.ads.googleads.v2.resources.AdGroup
import com.google.ads.googleads.v2.services.{AdGroupOperation, MutateAdGroupsResponse}
import com.google.ads.googleads.v2.utils.ResourceNames
import com.google.protobuf.{Int64Value, StringValue}
import model.PriceListRecord
import utils.google.AdsClientFactory

import collection.JavaConverters._

object AdGroupActor {
  def addAdGroups(customerId: Long, campaignId: Long, records: Iterable[PriceListRecord]): MutateAdGroupsResponse = {
    val adGroupOperations = records
      .map(record => AdGroup.newBuilder()
        .setName(StringValue.of(campaignId + " " + record.keyword + ' ' + '(' + new Date() + ')'))
        .setStatus(AdGroupStatus.ENABLED)
        .setCampaign(StringValue.of(ResourceNames.campaign(customerId, campaignId)))
        .setType(AdGroupType.SEARCH_STANDARD)
        .setCpcBidMicros(Int64Value.of(90000000))
        .build()
      )
      .map(AdGroupOperation.newBuilder().setCreate(_).build())
      .toList
      .asJava

    val client = AdsClientFactory.google.getLatestVersion.createAdGroupServiceClient()
    val response = client.mutateAdGroups(customerId.toString, adGroupOperations)
    client.shutdown()
    response
  }

  def props: Props = Props[AdGroupActor]

  final case class AddAdGroupsRequest(customerId: Long, campaignId: Long, records: Iterable[PriceListRecord])

  trait Factory {
    def apply(): Actor
  }

}

class AdGroupActor extends Actor {

  override def receive: Receive = {
    case AddAdGroupsRequest(customerId, campaignId, records) =>
      val response = AdGroupActor.addAdGroups(customerId, campaignId, records)
      sender() ! AddAdGroupsResponse(response)
  }
}
