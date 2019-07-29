package actors.inject

import actors.google.dispatchers.{DispatcherActor, PriceListActor}
import actors.google.workers._
import actors.google.workers.{AdGroupActor, CampaignActor, CampaignBudgetActor, ExpandedTextAdsActor}
import actors.workers.PriceListFileActor
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
  }
}
