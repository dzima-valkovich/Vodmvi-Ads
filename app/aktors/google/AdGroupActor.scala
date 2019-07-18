package aktors.google

import java.util.Date

import akka.actor.Actor
import com.google.ads.googleads.v2.enums.AdGroupStatusEnum.AdGroupStatus
import com.google.ads.googleads.v2.enums.AdGroupTypeEnum.AdGroupType
import com.google.ads.googleads.v2.resources.AdGroup
import com.google.ads.googleads.v2.services.AdGroupOperation
import com.google.ads.googleads.v2.utils.ResourceNames
import com.google.protobuf.{Int64Value, StringValue}
import model.PriceListRecord
import utils.google.AdsClientFactory

object AdGroupActor {

  final case class BuildAdGroup(record: PriceListRecord)

  final case class CreateAdGroups(records: Iterator[PriceListRecord], customerId: Long, campaignId: Long)

}

class AdGroupActor extends Actor {
//  private var adGroup: AdGroup = _

  import aktors.google.AdGroupActor.{BuildAdGroup, CreateAdGroups}
  import collection.JavaConverters._

  override def receive: Receive = {
    case CreateAdGroups(records, customerId, campaignId) =>
      val client = AdsClientFactory.google.getLatestVersion.createAdGroupServiceClient()

      val adGroupOperations = records
        .map(record => AdGroup.newBuilder().setName(StringValue.of(campaignId + ' ' + record.keyword + ' ' + '(' + new Date() + ')'))
          .setStatus(AdGroupStatus.ENABLED)
          .setCampaign(StringValue.of(ResourceNames.campaign(customerId, campaignId)))
          .setType(AdGroupType.SEARCH_STANDARD)
          .setCpcBidMicros(Int64Value.of(90000000))
          .build()
        ).map(AdGroupOperation.newBuilder().setCreate(_).build())
        .toList
        .asJava

      val response = client.mutateAdGroups(customerId.toString, adGroupOperations)
      client.shutdown()
      val newAdGroupIds = response.getResultsList.asScala.map(_.getResourceName.split('/')(3).toLong)

  }
}
