package controllers

import java.nio.file.Paths

import javax.inject.Inject
import play.api.libs.Files
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, ControllerComponents, MultipartFormData}

class Uploader @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  private val PathToAppWorkDirectory = "/home/vodmvi/ads"
  private val PathToClientsCsv = PathToAppWorkDirectory + "/clients/csv/"

  private val FileUploadedMessage = Json.parse("""{"message":"Your data has been uploaded."}""")

  def uploadCsvFile: Action[Files.TemporaryFile] = Action(parse.temporaryFile) {
    implicit request =>
      val uploadedTemporaryFile = request.body

      uploadedTemporaryFile
        .moveFileTo(Paths.get(PathToClientsCsv + uploadedTemporaryFile.path.getFileName), replace = true)

      Ok(FileUploadedMessage)
  }

  def uploadCsvFiles: Action[MultipartFormData[Files.TemporaryFile]] = Action(parse.multipartFormData) {
    implicit request =>
      request
        .body
        .files
        .foreach(file => file.ref.moveFileTo(Paths.get(PathToClientsCsv + file.filename), replace = true))

      Ok(FileUploadedMessage)
  }

}
