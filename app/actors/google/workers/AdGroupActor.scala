package actors.google.workers

import java.util.Date

import actors.google.workers.AdGroupActor.{AddAdGroupsRequest, AddAdGroupsResponse}
import akka.actor.{Actor, Props}
import com.google.ads.googleads.v2.services.AdGroupOperation
import model.PriceListRecord
import model.ads.{AdGroup, Campaign, Customer}
import utils.google.AdsClientFactory
import actors.google.implicits.AdGroupRich

import scala.collection.JavaConverters._

object AdGroupActor {

  def props: Props = Props[AdGroupActor]

  final case class AddAdGroupsRequest(customer: Customer, campaign: Campaign, records: Iterable[PriceListRecord])

  final case class AddAdGroupsResponse(adGroups: Iterable[AdGroup])

  trait Factory {
    def apply(): Actor
  }

}

class AdGroupActor extends Actor {
  private def addAdGroups(customer: Customer, campaign: Campaign, records: Iterable[PriceListRecord]): Iterable[AdGroup] = {
    val adGroups = records
      .map(priceList => AdGroup(customer, campaign, campaign.id + " " + priceList.keyword + '(' + new Date + ')', 900000))

    val adGroupOperations = adGroups
      .map(group => AdGroupOperation.newBuilder().setCreate(group.toGoogle).build())
      .toList
      .asJava

    val client = AdsClientFactory.google.getLatestVersion.createAdGroupServiceClient()
    val response = client.mutateAdGroups(customer.id.toString, adGroupOperations)
    client.shutdown()
    adGroups.zip(response.getResultsList.asScala)
      .map(tuple => {
        tuple._1.id = tuple._2.getResourceName.split('/')(3)
        tuple._1
      })
  }

  override def receive: Receive = {
    case AddAdGroupsRequest(customer: Customer, campaign: Campaign, records: Iterable[PriceListRecord]) =>
      val response = addAdGroups(customer, campaign, records)
      sender() ! AddAdGroupsResponse(response)
  }
}
