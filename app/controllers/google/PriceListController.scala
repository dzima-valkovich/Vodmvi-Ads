package controllers.google

import akka.actor.ActorSystem
import akka.util.Timeout
import aktors.google.PriceListActor
import aktors.google.PriceListActor.{ProcessPriceListRequest}
import com.google.ads.googleads.v2.common.{ExpandedTextAdInfo, Keyword, KeywordInfo}
import com.google.ads.googleads.v2.resources.{Ad, AdGroup, AdGroupAd, AdGroupCriterion}
import com.google.ads.googleads.v2.services.{AdGroupAdOperation, AdGroupCriterionOperation, AdGroupOperation}
import javax.inject.Inject
import play.api.libs.Files
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents, MultipartFormData}
import utils.CsvReader
import utils.google.AdsClientFactory
import utils.google.implicits._

import scala.concurrent.ExecutionContext.Implicits.global

class PriceListController @Inject()(system: ActorSystem, cc: ControllerComponents) extends AbstractController(cc) {
  private val googleClient = AdsClientFactory.google

  import collection.JavaConverters._
  import akka.pattern.ask
  import scala.concurrent.duration._

  def createAdsCampaignFromCsvPriceList(clientId: Long): Action[MultipartFormData[Files.TemporaryFile]] = Action(parse.multipartFormData).async {
    request =>
      implicit val timeout: Timeout = 10.seconds
      val actor = system.actorOf(PriceListActor.props(clientId))
      (actor ? ProcessPriceListRequest(request.body.files.head.ref.toFile))
        .mapTo[String]
        .map(Ok(_))
  }

  def createAdGroupFromCsv(clientId: Long, campaignId: Long): Action[MultipartFormData[Files.TemporaryFile]] = Action(parse.multipartFormData) {
    implicit request =>

      def sendGroupCreationRequest(csvStrArr: Seq[(Array[String], Array[String], Array[String], Array[String], Array[String], Array[String])]) = {
        val groupOperations: java.util.List[AdGroupOperation] =
          csvStrArr
            .map(arr => AdGroup.newBuilder().fromArray(arr._1, withDate = true)(clientId, campaignId).build())
            .map(AdGroupOperation.newBuilder().setCreate(_).build)
            .toList
            .asJava

        googleClient
          .getLatestVersion
          .createAdGroupServiceClient()
          .mutateAdGroups(clientId.toString, groupOperations)
      }

      def sendExpCreationRequest(csvStrArrAndGroupIdsTuple: Seq[((Array[String], Array[String], Array[String], Array[String], Array[String], Array[String]), Long)]) = {
        val expOperations =
          csvStrArrAndGroupIdsTuple
            .map(arr => {
              val exp = ExpandedTextAdInfo.newBuilder().fromArray(arr._1._2).build()
              val ad = Ad.newBuilder().fromArray(arr._1._3, exp).build()
              AdGroupAd.newBuilder().fromArray(arr._1._4, ad)(clientId, arr._2)
            })
            .map(AdGroupAdOperation.newBuilder().setCreate(_).build())
            .toList
            .asJava

        googleClient
          .getLatestVersion
          .createAdGroupAdServiceClient
          .mutateAdGroupAds(clientId.toString, expOperations)
      }

      def sendKeywordCreationRequest(csvStrArrAndGroupIdsTuple: Seq[((Array[String], Array[String], Array[String], Array[String], Array[String], Array[String]), Long)]) = {
        val criterionOperations =
          csvStrArrAndGroupIdsTuple
            .map(arr => {
              val keywordInfo = KeywordInfo.newBuilder().fromArray(arr._1._5).build()
              AdGroupCriterion.newBuilder().fromArray(arr._1._6, keywordInfo)(clientId, arr._2)
            })
            .map(AdGroupCriterionOperation.newBuilder().setCreate(_).build())
            .toList
            .asJava

        googleClient
          .getLatestVersion
          .createAdGroupCriterionServiceClient()
          .mutateAdGroupCriteria(clientId.toString, criterionOperations)
      }

      val csvStrArr =
        request.body
          .files
          .flatMap(file => CsvReader.readAllAndSlice(file.ref.toFile))

      val groupResponse = sendGroupCreationRequest(csvStrArr)

      val groupIds = groupResponse.getResultsList.asScala.map(_.getResourceName.split('/')(3).toLong)
      val csvStrArrAndGroupIdsTuple = csvStrArr.zip(groupIds)

      val expResponse = sendExpCreationRequest(csvStrArrAndGroupIdsTuple)
      val criterionResponse = sendKeywordCreationRequest(csvStrArrAndGroupIdsTuple)

      Ok(expResponse.toString + "\n\n" + criterionResponse.toString)
        .as(JSON)
  }

}
