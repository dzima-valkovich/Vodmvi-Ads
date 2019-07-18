package aktors.google

import akka.actor.Actor
import aktors.google.ExpandedTextAdsActor.CreateExpandedTexts
import model.PriceListRecord

object ExpandedTextAdsActor {

  final case class CreateExpandedTexts(tuple: Iterator[(PriceListRecord, Long)])

}

class ExpandedTextAdsActor extends Actor {

  import aktors.google.ExpandedTextAdsActor.CreateExpandedTexts

  override def receive: Receive = {
    case CreateExpandedTexts(tuple) =>
  }

}
