package actors.inject

import actors.google.auth.Oauth2Actor
import actors.google.dispatchers.{DispatcherActor, PriceListActor}
import actors.google.workers._
import actors.google.workers.{AdGroupActor, CampaignActor, CampaignBudgetActor, ExpandedTextAdsActor}
import actors.workers.PriceListFileActor
import akka.routing.RoundRobinPool
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

class VodmviInjectModule extends AbstractModule with AkkaGuiceSupport {
  override def configure(): Unit = {
    bindActor[DispatcherActor]("DispatcherActor")

    bindActorFactory[PriceListActor, PriceListActor.Factory]
    bindActorFactory[PriceListFileActor, PriceListFileActor.Factory]
    bindActorFactory[CampaignBudgetActor, CampaignBudgetActor.Factory]
    bindActorFactory[CampaignActor, CampaignActor.Factory]
    bindActorFactory[AdGroupActor, AdGroupActor.Factory]
    bindActorFactory[ExpandedTextAdsActor, ExpandedTextAdsActor.Factory]
    bindActorFactory[KeywordActor, KeywordActor.Factory]

    bindActor[Oauth2Actor]("Oauth2Actor", props => RoundRobinPool(15).props(props))
    bindActor[CustomerActor]("CustomerActor", props => RoundRobinPool(15).props(props))
  }
}
