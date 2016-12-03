package net.fast2smart.simulation

import java.time.LocalDateTime
import java.time.temporal.ChronoField

import io.gatling.core.feeder.Feeder

import scala.util.Random

class PurchaseFeeder extends Feeder[Number] {
  private val random = new Random

  override def hasNext = true

  override def next(): Map[String, Number] = {
    val amount = Math.round((random.nextFloat() + random.nextInt(100).abs.toFloat) * 100) / 100.0
    val basePoints = amount.toLong / 2
    val statusPoints = basePoints * random.nextInt(11) // max 10-fach
    Map("amount" -> amount, "basePoints" -> basePoints, "statusPoints" -> statusPoints)
  }
}


case class GS1(value: Long) {
  def create: Long = {
    val sValue = value.toString
    val checksum: Int = 10 - (sValue.sliding(2, 2).map(_.toInt).map(k => k / 10 + k % 10 * 3).sum % 10) % 10
    (sValue + (if (checksum == 10) 0 else checksum)).toLong
  }
}

class CardnumberFeeder extends Feeder[Long] {
  private val random = new Random

  override def hasNext = true

  override def next: Map[String, Long] = {
    val card = java.lang.Long.parseLong(s"320${random.nextInt(999999999).abs}".padTo(12, "0").mkString)
    Map("cardnumber" -> GS1(card).create)
  }
}

class DateFeeder(name: String, lowerBound: Int, upperBound: Int) extends Feeder[LocalDateTime] {

  import java.time.LocalDateTime

  private val random = new Random

  // always return true as this feeder can be polled infinitively
  override def hasNext = true

  override def next: Map[String, LocalDateTime] = {
    val year = randInt(lowerBound, upperBound)
    val month = randInt(1, 12)
    val day = randInt(1, daysOfMonth(year, month))

    Map(name -> LocalDateTime.of(year, month, day, randInt(1, 24), randInt(0, 60)))
  }

  // random number in between [a...b]
  private def randInt(a: Int, b: Int) = random.nextInt(b - a) + a

  private def daysOfMonth(year: Int, month: Int) = LocalDateTime.of(year, month, 1, 0, 0).range(ChronoField.DAY_OF_MONTH).getMaximum.toInt
}