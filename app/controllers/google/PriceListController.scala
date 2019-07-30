package controllers.google

import actors.google.dispatchers.DispatcherActor.ProcessPriceListRequest
import akka.actor.ActorRef
import akka.util.Timeout
import javax.inject.{Inject, Named}
import model.ads.Customer
import play.api.libs.Files
import play.api.mvc.{AbstractController, Action, ControllerComponents, MultipartFormData}

import scala.concurrent.ExecutionContext.Implicits.global

class PriceListController @Inject()(
                                     @Named("DispatcherActor") dispatcherActorRef: ActorRef,
                                     cc: ControllerComponents
                                   )
  extends AbstractController(cc) {

  import collection.JavaConverters._
  import akka.pattern.ask
  import scala.concurrent.duration._

  def createAdsCampaignFromCsvPriceList(clientId: String, refreshToken: String, loginCustomerId: String): Action[MultipartFormData[Files.TemporaryFile]] = Action(parse.multipartFormData).async {
    request =>
      implicit val timeout: Timeout = 10.seconds
      (dispatcherActorRef ? ProcessPriceListRequest(Customer(
        id = Some(clientId)
        , refreshToken = Some(refreshToken)
        , loginCustomerId = Some(loginCustomerId))
        , request.body.files.head.ref.toFile))
        .mapTo[String]
        .map(Ok(_))
  }
}
