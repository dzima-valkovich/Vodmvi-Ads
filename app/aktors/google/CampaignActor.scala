package aktors.google

import akka.actor.Actor

object CampaignActor {

  final case class AddCampaign()

}

class CampaignActor extends Actor {

  override def receive: Receive = {

  }

}
