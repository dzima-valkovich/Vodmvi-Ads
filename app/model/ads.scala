package model

import java.util.Date

object ads {

  trait Resource {
    var id: String = _
  }

  case class Customer() extends AnyRef with Resource {
    def this(id: String) = {
      this()
      this.id = id
    }
  }

  case class AdGroup(customer: Customer, campaign: Campaign, name: String, cpc: Long) extends AnyRef with Resource

  case class Campaign(customer: Customer, budget: CampaignBudget, name: String, startDate: Date, duration: Int) extends AnyRef with Resource

  case class CampaignBudget(customer: Customer, name: String, amount: Long) extends AnyRef with Resource

  case class Keyword(customer: Customer, adGroup: AdGroup, keyword: String) extends AnyRef with Resource

  case class Ad(customer: Customer, adGroup: AdGroup, headline1: String, headline2: String, description: String, url: String) extends AnyRef with Resource

}
