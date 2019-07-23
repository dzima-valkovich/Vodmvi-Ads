package actors.google.workers

import actors.google.workers.KeywordActor.{AddKeywordsRequest, AddKeywordsResponse}
import akka.actor.{Actor, Props}
import com.google.ads.googleads.v2.services.AdGroupCriterionOperation
import model.PriceListRecord
import model.ads.{AdGroup, Customer, Keyword}
import utils.google.AdsClientFactory
import actors.google.implicits.KeywordRich

import scala.collection.JavaConverters._

object KeywordActor {
  def props: Props = Props[KeywordActor]

  final case class AddKeywordsRequest(customer: Customer, tuples: Iterable[(PriceListRecord, AdGroup)])

  final case class AddKeywordsResponse(keywords: Iterable[Keyword])

  trait Factory {
    def apply(): Actor
  }

}

class KeywordActor extends Actor {
  private def addKeywords(customer: Customer, tuples: Iterable[(PriceListRecord, AdGroup)]): Iterable[Keyword] = {
    val keywords = tuples.map(t => Keyword(customer, t._2, t._1.keyword))

    val criterionOperations = keywords
      .map(keyword => AdGroupCriterionOperation.newBuilder().setCreate(keyword.toGoogle).build())
      .toList
      .asJava

    val client = AdsClientFactory.google.getLatestVersion.createAdGroupCriterionServiceClient()
    val response = client.mutateAdGroupCriteria(customer.id.toString, criterionOperations)
    client.shutdown()
    keywords.zip(response.getResultsList.asScala)
      .map(t => {
        t._1.id = t._2.getResourceName.split('/')(3)
        t._1
      })
  }

  override def receive: Receive = {
    case AddKeywordsRequest(customer, tuples) =>
      val response = addKeywords(customer, tuples)
      sender ! AddKeywordsResponse(response)
  }
}
