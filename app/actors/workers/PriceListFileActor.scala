package actors.workers

import java.io.File

import actors.workers.PriceListFileActor.{ReadFromCsvRequest, ReadFromCsvResponse}
import akka.actor.{Actor, Props}
import model.PriceListRecord

import scala.io.Source

object PriceListFileActor {

  def props: Props = Props[PriceListFileActor]

  final case class ReadFromCsvRequest(file: File)

  case class ReadFromCsvResponse(priceList: Iterable[PriceListRecord])

  trait Factory {
    def apply(): Actor
  }

}

class PriceListFileActor extends Actor {
  private def readPriceList(csvFile: File): Iterable[PriceListRecord] =
    Source
      .fromFile(csvFile)
      .getLines()
      .map(str => {
        val record = str.split(',')
        PriceListRecord(record(0), record(1).toDouble)
      }).toIterable

  override def receive: Receive = {
    case ReadFromCsvRequest(file: File) =>
      val records = readPriceList(file)
      sender() ! ReadFromCsvResponse(records)
  }
}
