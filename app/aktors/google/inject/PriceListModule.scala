package aktors.google.inject

import aktors.PriceListFileActor
import aktors.google.{AdGroupActor, CampaignActor, CampaignBudgetActor, ExpandedTextAdsActor, KeywordActor, PriceListActor}
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

class PriceListModule extends AbstractModule with AkkaGuiceSupport {
  override def configure(): Unit = {
    bindActor[PriceListActor]("PriceListActor")

    bindActorFactory[PriceListFileActor, PriceListFileActor.Factory]
    bindActorFactory[CampaignBudgetActor, CampaignBudgetActor.Factory]
    bindActorFactory[CampaignActor, CampaignActor.Factory]
    bindActorFactory[AdGroupActor, AdGroupActor.Factory]
    bindActorFactory[ExpandedTextAdsActor, ExpandedTextAdsActor.Factory]
    bindActorFactory[KeywordActor, KeywordActor.Factory]
  }
}
