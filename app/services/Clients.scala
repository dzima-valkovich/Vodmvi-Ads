package services

import entities.Client

object Clients {
  def apply[T](login: Long): Client = {
    val c = Client(123)
    c.loadedGroupsCsvFilePaths = List("/home/vodmvi/ads/clients/csv/company1.csv")
    c
  }
}
