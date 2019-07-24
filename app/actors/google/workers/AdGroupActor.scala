package actors.google.workers

import actors.google.workers.AdGroupActor.{AddAdGroupsRequest, AddAdGroupsResponse}
import akka.actor.Actor
import com.google.ads.googleads.v2.services.AdGroupOperation
import model.ads.{AdGroup, Customer}
import utils.google.AdsClientFactory
import actors.google.implicits.AdGroupRich

import scala.collection.JavaConverters._

object AdGroupActor {

  final case class AddAdGroupsRequest(customer: Customer, adGroups: Iterable[AdGroup])

  final case class AddAdGroupsResponse(adGroups: Iterable[AdGroup])

  trait Factory {
    def apply(): Actor
  }

}

class AdGroupActor extends Actor {
  private def addAdGroups(customer: Customer, adGroups: Iterable[AdGroup]): Iterable[AdGroup] = {
    val adGroupOperations = adGroups
      .map(group => AdGroupOperation.newBuilder().setCreate(group.toGoogle).build())
      .toList
      .asJava

    val client = AdsClientFactory.google.getLatestVersion.createAdGroupServiceClient()
    val response = client.mutateAdGroups(customer.id.get, adGroupOperations)
    client.shutdown()
    adGroups.zip(response.getResultsList.asScala)
      .map(tuple => tuple._1.copy(id = Some(tuple._2.getResourceName.split('/')(3))))
  }

  override def receive: Receive = {
    case AddAdGroupsRequest(customer: Customer, adGroups: Iterable[AdGroup]) =>
      val response = addAdGroups(customer, adGroups)
      sender() ! AddAdGroupsResponse(response)
  }
}
