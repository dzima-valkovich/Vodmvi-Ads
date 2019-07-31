package model

import java.util.Date

object ads {

  case class Resource(id: Option[String] = None)

  case class Customer(id: Option[String] = None, refreshToken: Option[String] = None,
                      email: Option[String] = None, loginCustomerId: Option[String] = None)

  case class AdGroup(customer: Customer, campaign: Campaign, name: String, id: Option[String] = None)

  case class Campaign(customer: Customer, budget: CampaignBudget, name: String, startDate: Date, duration: Int, id: Option[String] = None)

  case class CampaignBudget(customer: Customer, name: String, amount: Long, id: Option[String] = None)

  case class Keyword(customer: Customer, adGroup: AdGroup, keyword: String, id: Option[String] = None)

  case class Ad(customer: Customer, adGroup: AdGroup, headline1: String, headline2: String, description: String, url: String, id: Option[String] = None)

}
