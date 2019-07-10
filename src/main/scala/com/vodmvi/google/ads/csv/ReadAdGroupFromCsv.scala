package com.vodmvi.google.ads.csv

import java.io.Reader

import com.google.ads.googleads.v2.enums.AdGroupTypeEnum.AdGroupType
import com.google.ads.googleads.v2.enums.AdGroupStatusEnum.AdGroupStatus
import com.google.ads.googleads.v2.resources.AdGroup
import com.google.protobuf.{Int64Value, StringValue}
import com.opencsv.{CSVReader, CSVReaderBuilder}

object AdGroupCsvBuilder {


  def apply(csvFileReader: Reader): List[AdGroup] = {
    val csvReader = new CSVReader(csvFileReader)
    val adGroupStringData = csvReader.readAll()
    for (adGroupRecord: Array[String] <- adGroupStringData)
      yield AdGroup.newBuilder()
        .setName(StringValue.of(adGroupRecord(0)))
        .setStatus(AdGroupStatus.valueOf(adGroupRecord(1)))
        .setType(AdGroupType.valueOf(adGroupRecord(2)))
        .setCpcBidMicros(Int64Value.of(adGroupRecord(3).toLong))
  }
}
