package actors.google.workers

import actors.google.workers.CustomerActor.{ListAccessibleCustomersRequest, ListAccessibleCustomersResponse}
import akka.actor.Actor
import javax.inject.Inject
  import model.ads.Customer
import utils.google.AdsClientFactory
import collection.JavaConverters._

object CustomerActor {

  final case class ListAccessibleCustomersRequest(customer: Customer)

  final case class ListAccessibleCustomersResponse(customers: Iterable[Customer])

}

class CustomerActor @Inject()(
                               adsClientFactory: AdsClientFactory
                             )
  extends Actor {

  override def receive: Receive = {
    case ListAccessibleCustomersRequest(customer) =>
      import com.google.ads.googleads.v2.services.ListAccessibleCustomersRequest
      val response = adsClientFactory
        .google(customer.refreshToken.get)
        .getLatestVersion
        .createCustomerServiceClient()
        .listAccessibleCustomers(ListAccessibleCustomersRequest.newBuilder().build())

      sender() ! ListAccessibleCustomersResponse(response
        .getResourceNamesList
        .asScala
        .map(resourceUri => Customer(Some(resourceUri.split('/')(1)), customer.refreshToken, customer.email)))
  }
}
