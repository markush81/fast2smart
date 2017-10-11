
package net.fast2smart.simulation

import io.gatling.core.Predef._
import io.gatling.core.feeder.RecordSeqFeederBuilder
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder
import io.gatling.http.request.builder.HttpRequestBuilder
import io.gatling.jdbc.Predef._

import scala.util.Random

/**
  * Enrol several members and do a bunch of historical purchases for each one.
  */
class Historic extends Simulation {
  val firstnames: RecordSeqFeederBuilder[String] = csv("firstnames.csv").random
  val lastnames: RecordSeqFeederBuilder[String] = csv("lastnames.csv").random
  val partners: RecordSeqFeederBuilder[String] = csv("partners.csv").random
  val cardnumbers: CardnumberFeeder = new CardnumberFeeder
  val enrolmentDate: DateFeeder = new DateFeeder("enrolmentDate", 1990, 2014)
  val purchaseDate: DateFeeder = new DateFeeder("purchaseDate", 1990, 2014)
  val purchaseData: PurchaseFeeder = new PurchaseFeeder
  val random: Random = new Random
  val purchaseCounter: Iterator[Map[String, Int]] = Iterator.continually(Map("purchaseCount" -> random.nextInt(5).abs))

  val httpConf: HttpProtocolBuilder = http
    .baseURL("http://localhost:8080")
    .contentTypeHeader("application/json;charset=UTF-8")

  val enrollMember: HttpRequestBuilder = http("EnrolMember")
    .post("/members")
    .body(StringBody("""{"lastname":"${lastname}","firstname":"${firstname}","card":{"number":${cardnumber},"partner":"${partner}"}, "enrolmentDate": "${enrolmentDate}"}}""")).asJSON
    .check(status.is(200))
    .check(jsonPath("$.card.number").saveAs("membercard"))

  val purchase: HttpRequestBuilder = http("Purchase")
    .post("/purchases")
    .body(StringBody("""{"cardnumber":${membercard},"partner":"${partner}","amount":"${amount}","currency":"EUR","basePoints":${basePoints},"statusPoints":${statusPoints}, "date":"${purchaseDate}"}""")).asJSON
    .check(status.is(200))

  val scn: ScenarioBuilder = scenario("Historical Data Simulation")
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
  val partners: RecordSeqFeederBuilder[String] = csv("partners.csv").random
  val purchaseDate: DateFeeder = new DateFeeder("purchaseDate", 2015, 2016)
  val purchaseData: PurchaseFeeder = new PurchaseFeeder
  val random: Random = new Random
  val purchaseCounter: Iterator[Map[String, Int]] = Iterator.continually(Map("purchaseCount" -> random.nextInt(100).abs))
  val memberFeeder: RecordSeqFeederBuilder[Any] = jdbcFeeder("jdbc:h2:./db/fast2smart;AUTO_SERVER=TRUE", "sa", "", "SELECT CARDNUMBER FROM MEMBER")

  val httpConf: HttpProtocolBuilder = http
    .baseURL("http://localhost:8080")
    .contentTypeHeader("application/json;charset=UTF-8")

  val purchase: HttpRequestBuilder = http("Purchase")
    .post("/purchases")
    .body(StringBody("""{"cardnumber":${CARDNUMBER},"partner":"${partner}","amount":"${amount}","currency":"EUR","basePoints":${basePoints},"statusPoints":${statusPoints}, "date":"${purchaseDate}"}""")).asJSON
    .check(status.is(200))

  val scn: ScenarioBuilder = scenario("Current Purchases Simulation")
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

