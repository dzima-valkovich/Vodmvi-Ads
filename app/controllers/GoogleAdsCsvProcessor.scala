package controllers

import java.io.{File, FileReader}

import com.google.ads.googleads.v2.resources.AdGroup
import com.google.ads.googleads.v2.services.AdGroupOperation
import javax.inject.Inject
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import utils.google.implicits._
import services.Clients
import io.grpc.StatusRuntimeException
import play.api.libs.json.Json
import utils.google.AdsClientFactory

import collection.JavaConverters._

class GoogleAdsCsvProcessor @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  private val googleClient = AdsClientFactory.google

  def addGroupsFromCsvFile(clientId: Long): Action[AnyContent] = Action {
    implicit request =>
      val operations = Clients(clientId)
        .loadedGroupsCsvFilePaths
        .flatMap(path => AdGroup.newBuilder.fromCsv(new File(path))(clientId).build)
        .map(adGroup => AdGroupOperation.newBuilder().setCreate(adGroup).build)
        .toList
        .asJava

      try {
        val response = googleClient.getLatestVersion.createAdGroupServiceClient()
          .mutateAdGroups(clientId.toString, operations)

        Ok(response.toString)
          .as(JSON)
      } catch {
        case e: RuntimeException => BadRequest(e.toString)
          .as(JSON)
      }
  }

}
