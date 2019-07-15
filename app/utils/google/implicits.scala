package utils.google

import java.io.File
import java.util.Date

import com.google.ads.googleads.v2.enums.AdGroupStatusEnum.AdGroupStatus
import com.google.ads.googleads.v2.enums.AdGroupTypeEnum.AdGroupType
import com.google.ads.googleads.v2.resources.AdGroup
import com.google.ads.googleads.v2.resources.AdGroup.Builder
import com.google.ads.googleads.v2.utils.ResourceNames
import com.google.protobuf.{Int64Value, StringValue}

import scala.io.Source

object implicits {

  implicit class AdGroupBuilderIterator(val iterator: Iterator[Builder]) {

    def build: Iterator[AdGroup] = iterator.map(_.build())
  }

  implicit class AdGroupBuilderRich(val builder: Builder) {

    def fromCsv(csvFile: File, withDate: Boolean = true)(customerId: Long): Iterator[Builder] =
      AdGroupCsvReader.readAll(csvFile, withDate)(customerId)
  }


  //  implicit def userToClient(user: User): Client = user.asInstanceOf[Client]


  //  implicit def toAdGroupRich(adGroup: AdGroup): AdGroupRich = AdGroupRich(adGroup)
}
