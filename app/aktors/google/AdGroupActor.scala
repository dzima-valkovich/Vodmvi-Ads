package aktors.google

import java.util.Date

import akka.actor.{Actor, Props}
import aktors.PriceListActor.Report
import aktors.google.ExpandedTextAdsActor.AddExpandedTexts
import com.google.ads.googleads.v2.enums.AdGroupStatusEnum.AdGroupStatus
import com.google.ads.googleads.v2.enums.AdGroupTypeEnum.AdGroupType
import com.google.ads.googleads.v2.resources.AdGroup
import com.google.ads.googleads.v2.services.{AdGroupOperation, MutateAdGroupsResponse}
import com.google.ads.googleads.v2.utils.ResourceNames
import com.google.protobuf.{Int64Value, StringValue}
import model.PriceListRecord
import utils.google.AdsClientFactory

object AdGroupActor {
  def addAdGroup(customerId: Long, campaignId: Long, records: Iterator[PriceListRecord]): MutateAdGroupsResponse = {
  val adGroupOperations = records
    .map(record => AdGroup.newBuilder().setName(StringValue.of(campaignId + ' ' + record.keyword + ' ' + '(' + new Date() + ')'))
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

  final case class AddAdGroups(customerId: Long, campaignId: Long, records: Iterator[PriceListRecord])

}

class AdGroupActor extends Actor {



  override def receive: Receive = {
    case AddAdGroups(customerId, campaignId, records) =>
      val response = addAdGroup(customerId, campaignId, records)
      sender ! Report(("adGroupResponse", response), 1)

    case AddAdGroupAndExpandedTextAdsAndKeyword(customerId: Long, campaignId: Long, records: Iterator[PriceListRecord]) =>
      val groupResponse = addAdGroup(customerId, campaignId, records)
      sender ! Report(("groupResourceNames", None), 1)
      val data = records.zip(groupResponse.getResultsList.asScala.map(_.getResourceName).iterator)

      val expandedTextAdsActor = context.actorOf(ExpandedTextAdsActor.props, "ExpandedTextAdsActor")
      expandedTextAdsActor ! AddExpandedTexts(customerId, data)

      val
  }
}
