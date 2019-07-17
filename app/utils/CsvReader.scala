package utils

import java.io.File

import model.PriceListRecord

import scala.io.Source

object CsvReader {
  private val groupBounds = (0, 4)
  private val expandedTextBounds = (4, 7)
  private val adBounds = (7, 8)
  private val adGroupAdBounds = (8, 9)
  private val keywordBound = (9, 11)
  private val criterionBoud = (11, 12)

  def readPriceList(csvFile: File): Iterator[PriceListRecord] =
    Source
      .fromFile(csvFile)
      .getLines()
      .map(str => {
        val record = str.split(',')
        PriceListRecord(record(0), record(1).toDouble)
      })


  def readAll(csvFile: File): Iterator[Array[String]] = {
    Source
      .fromFile(csvFile)
      .getLines()
      .map(_.split(','))
  }

  def readAllAndSlice(csvFile: File): Iterator[(Array[String], Array[String], Array[String], Array[String], Array[String], Array[String])] = {
    readAll(csvFile)
      .map(strArr => (strArr.slice(groupBounds._1, groupBounds._2)
        , strArr.slice(expandedTextBounds._1, expandedTextBounds._2)
        , strArr.slice(adBounds._1, adBounds._2)
        , strArr.slice(adGroupAdBounds._1, adGroupAdBounds._2)
        , strArr.slice(keywordBound._1, keywordBound._2)
        , strArr.slice(criterionBoud._1, criterionBoud._2)))
  }

}
