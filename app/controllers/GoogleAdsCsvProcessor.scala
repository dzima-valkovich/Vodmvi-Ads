package controllers

import java.io.{File, FileReader}

import com.google.ads.googleads.v2.services.AdGroupOperation
import javax.inject.Inject
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import entities.implicits._
import services.Clients
import google.utils.{AdGroupCsvReader, AdsClientFactory}
import io.grpc.StatusRuntimeException
import play.api.libs.json.Json

import collection.JavaConverters._

class CsvProcessor @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  private val client = AdsClientFactory()

  def processGroupCsv(clientId: Long): Action[AnyContent] = Action {
    implicit request =>
      val operations = Clients(clientId).loadedGroupsCsvFilePaths
        .flatMap(path => AdGroupCsvReader(new File(path))(clientId))
        .map(adGroup => AdGroupOperation.newBuilder().setCreate(adGroup).build)
        .toList.asJava

      try {
        val response = client.getLatestVersion.createAdGroupServiceClient()
          .mutateAdGroups(clientId.toString, operations)

        //        Ok(Json.parse(response.toString))
        Ok(response.toString)
          .as("application/json")
      } catch {
        case e: RuntimeException => BadRequest(e.toString)
          .as("application/json")
      }

  }

}
