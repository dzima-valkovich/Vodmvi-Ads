package aktors

import java.io.File

import akka.actor.{Actor, Props}
import aktors.PriceListFileActor.{ReadFromCsvRequest, ReadFromCsvResponse}
import model.PriceListRecord
import utils.CsvReader

object PriceListFileActor {

  def props: Props = Props[PriceListFileActor]

  final case class ReadFromCsvRequest(file: File)

  case class ReadFromCsvResponse(priceList: Iterable[PriceListRecord])

  trait Factory {
    def apply(): Actor
  }

}

class PriceListFileActor extends Actor {

  override def receive: Receive = {
    case ReadFromCsvRequest(file: File) =>
      val records = CsvReader.readPriceList(file)
      sender() ! ReadFromCsvResponse(records)
  }
}
