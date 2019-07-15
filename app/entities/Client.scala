package entities

object Client {
//  def apply(): Client = new Client()

  def apply(clientId: Long): Client = new Client(clientId)
}

class Client(val clientId: Long) {
  var loadedCampaingsCsvFilePaths: Seq[String] = _
  var loadedGroupsCsvFilePaths: Seq[String] = _
  var loadedKeywordsCsvFilePaths: Seq[String] = _

}
