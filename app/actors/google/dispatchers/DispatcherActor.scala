package actors.google.dispatchers

import java.io.File

import actors.google.dispatchers.DispatcherActor.ProcessPriceListRequest
import akka.actor.{Actor, ActorRef}
import javax.inject.Inject
import model.ads.Customer
import play.api.libs.concurrent.InjectedActorSupport

object DispatcherActor {

  final case class ProcessPriceListRequest(customer: Customer, priceListFile: File)

  final case class ProcessPriceListResponse()

}

class DispatcherActor @Inject()(priceListActorFactory: PriceListActor.Factory) extends Actor with InjectedActorSupport {

  private def priceListActorRef(customer: Customer): ActorRef =
    injectedChild(priceListActorFactory(customer), "PriceListActor")

  override def receive: Receive = {
    case ProcessPriceListRequest(customer, priceListFile) =>
      import actors.google.dispatchers.PriceListActor.ProcessPriceListRequest
      priceListActorRef(customer) forward ProcessPriceListRequest(priceListFile)
  }
}
