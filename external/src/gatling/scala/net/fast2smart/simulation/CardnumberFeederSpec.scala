package net.fast2smart.simulation

import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by markus on 11/11/2016.
  */
class CardnumberFeederSpec extends FlatSpec with Matchers {


  "CardnumberFeeder" should "give a valid EAN13 number" in {
    val feeder: CardnumberFeeder = new CardnumberFeeder
    val cardnumber: String = feeder.next.get("cardnumber").get.toString
    cardnumber should startWith("320")
    cardnumber should endWith(s"${GS1(java.lang.Long.parseLong(cardnumber.subSequence(0, 12).toString)).create}".charAt(12).toString)
    cardnumber should have length 13
  }

  "GS1" should "give correct EAN13" in {
    GS1(320451484714L).create.toString should have length 13
  }

}
