package actors.google.workers

import actors.google.workers.KeywordActor.{AddKeywordsRequest, AddKeywordsResponse}
import akka.actor.{Actor, Props}
import com.google.ads.googleads.v2.services.AdGroupCriterionOperation
import model.ads.{Customer, Keyword}
import utils.google.AdsClientFactory
import actors.google.implicits.KeywordRich
import actors.google.implicits.AdsClientFactoryRich
import javax.inject.Inject

import scala.collection.JavaConverters._

object KeywordActor {
  def props: Props = Props[KeywordActor]

  final case class AddKeywordsRequest(customer: Customer, keywords: Iterable[Keyword])

  final case class AddKeywordsResponse(keywords: Iterable[Keyword])

  trait Factory {
    def apply(): Actor
  }

}

class KeywordActor @Inject()(adsClientFactory: AdsClientFactory) extends Actor {
  private def addKeywords(customer: Customer, keywords: Iterable[Keyword]): Iterable[Keyword] = {
    val criterionOperations = keywords
      .map(keyword => AdGroupCriterionOperation.newBuilder().setCreate(keyword.toGoogle).build())
      .toList
      .asJava

    val client = adsClientFactory.google(customer).getLatestVersion.createAdGroupCriterionServiceClient()
    val response = client.mutateAdGroupCriteria(customer.id.get, criterionOperations)
    client.shutdown()
    keywords.zip(response.getResultsList.asScala)
      .map(t => t._1.copy(id = Some(t._2.getResourceName.split('/')(3))))
  }

  override def receive: Receive = {
    case AddKeywordsRequest(customer, keywords) =>
      val response = addKeywords(customer, keywords)
      sender ! AddKeywordsResponse(response)
  }
}
