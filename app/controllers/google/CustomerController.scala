package controllers.google

import actors.google.workers.CustomerActor.{ListAccessibleCustomersRequest, ListAccessibleCustomersResponse}
import akka.actor.ActorRef
import javax.inject.{Inject, Named}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import utils.google.AdsClientFactory
import akka.pattern.ask
import model.ads.Customer
import scala.concurrent.ExecutionContext.Implicits.global
import akka.util.Timeout
import play.api.libs.json.Json
import scala.concurrent.duration._

class CustomerController @Inject()(
                                    cc: ControllerComponents,
                                    adsClientFactory: AdsClientFactory,
                                    @Named("CustomerActor") customerActorRef: ActorRef
                                  )
  extends AbstractController(cc) {

  private val CustomerIdsFieldName = "customersIds"

  def listAccessibleCustomers(refreshToken: String): Action[AnyContent] = Action.async {
    request =>
      implicit val timeout: Timeout = 10.seconds
      (customerActorRef ? ListAccessibleCustomersRequest(Customer(refreshToken = Some(refreshToken))))
        .mapTo[ListAccessibleCustomersResponse]
        .map(response => {
          val a = response.customers.map(_.id.get)

          Ok(Json.obj(CustomerIdsFieldName -> a))
        })
  }
}
