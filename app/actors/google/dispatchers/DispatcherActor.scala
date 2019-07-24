package actors.google.dispatchers

import java.io.File

import actors.google.dispatchers.DispatcherActor.ProcessPriceListRequest
import actors.google.dispatchers.PriceListActor.ProcessPriceListResponse
import akka.actor.{Actor, ActorRef}
import javax.inject.Inject
import model.ads.Customer
import play.api.libs.concurrent.InjectedActorSupport

object DispatcherActor {

  final case class ProcessPriceListRequest(customerId: String, priceListFile: File)

  final case class ProcessPriceListResponse()

}

class DispatcherActor @Inject()(priceListActorFactory: PriceListActor.Factory) extends Actor with InjectedActorSupport {

  private def priceListActorRef(customerId: String): ActorRef =
    injectedChild(priceListActorFactory(Customer(Some(customerId))), "PriceListActor")

  override def receive: Receive = {
    case ProcessPriceListRequest(customerId, priceListFile) =>
      import actors.google.dispatchers.PriceListActor.ProcessPriceListRequest
      priceListActorRef(customerId) forward ProcessPriceListRequest(priceListFile)
  }
}
