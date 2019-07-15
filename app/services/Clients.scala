package services

import entities.{Client, User}

object Users {
  def apply[T](login: String): User = {
    val c = Client()
    c.loadedGroupsCsvFilePaths = List("/home/vodmvi/ads/clients/csv/company1.csv")
    c
  }
}
