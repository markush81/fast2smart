
package net.fast2smart.simulation

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

import scala.util.Random

/**
  * Enrol several members and do a bunch of historical purchases for each one.
  */
class Historic extends Simulation {
  val firstnames = csv("firstnames.csv").random
  val lastnames = csv("lastnames.csv").random
  val partners = csv("partners.csv").random
  val cardnumbers = new CardnumberFeeder
  val enrolmentDate = new DateFeeder("enrolmentDate", 1990, 2014)
  val purchaseDate = new DateFeeder("purchaseDate", 1990, 2014)
  val purchaseData = new PurchaseFeeder
  val random = new Random
  val purchaseCounter = Iterator.continually(Map("purchaseCount" -> random.nextInt(5).abs))

  val httpConf = http
    .baseURL("http://localhost:8080")
    .contentTypeHeader("application/json;charset=UTF-8")

  val enrollMember = http("EnrolMember")
    .post("/members")
    .body(StringBody("""{"lastname":"${lastname}","firstname":"${firstname}","card":{"number":${cardnumber},"partner":"${partner}"}, "enrolmentDate": "${enrolmentDate}"}}""")).asJSON
    .check(status.is(200))
    .check(jsonPath("$.card.number").saveAs("membercard"))

  val purchase = http("Purchase")
    .post("/purchases")
    .body(StringBody("""{"cardnumber":${membercard},"partner":"${partner}","amount":"${amount}","currency":"EUR","basePoints":${basePoints},"statusPoints":${statusPoints}, "date":"${purchaseDate}"}""")).asJSON
    .check(status.is(200))

  val scn = scenario("Historical Data Simulation")
    .during(60) {
      feed(firstnames).
        feed(lastnames).
        feed(partners).
        feed(cardnumbers).
        feed(enrolmentDate).
        feed(purchaseCounter).
        exec(enrollMember).
        repeat("${purchaseCount}") {
          feed(purchaseData).
            feed(purchaseDate).
            exec(purchase)
        }
    }

  setUp(
    scn.inject(atOnceUsers(20))
  ).protocols(httpConf)
}

/**
  * Do purchases for already enroled members.
  */
class Current extends Simulation {
  val partners = csv("partners.csv").random
  val purchaseDate = new DateFeeder("purchaseDate", 2015, 2016)
  val purchaseData = new PurchaseFeeder
  val random = new Random
  val purchaseCounter = Iterator.continually(Map("purchaseCount" -> random.nextInt(100).abs))
  val memberFeeder = jdbcFeeder("jdbc:h2:./db/fast2smart;AUTO_SERVER=TRUE", "sa", "", "SELECT CARDNUMBER FROM MEMBER")

  val httpConf = http
    .baseURL("http://localhost:8080")
    .contentTypeHeader("application/json;charset=UTF-8")

  val purchase = http("Purchase")
    .post("/purchases")
    .body(StringBody("""{"cardnumber":${CARDNUMBER},"partner":"${partner}","amount":"${amount}","currency":"EUR","basePoints":${basePoints},"statusPoints":${statusPoints}, "date":"${purchaseDate}"}""")).asJSON
    .check(status.is(200))

  val scn = scenario("Current Purchases Simulation")
    .during(60) {
      feed(partners).
        feed(memberFeeder).
        feed(purchaseCounter).
        repeat("${purchaseCount}") {
          feed(purchaseData).
            feed(purchaseDate).
            exec(purchase)
        }
    }

  setUp(
    scn.inject(atOnceUsers(20))
  ).protocols(httpConf)
}

