
package lv.arvissk.governor.base.modules.processing

import akka.actor._
import cakesolutions.kafka.akka.KafkaConsumerActor.Confirm
import cakesolutions.kafka.akka.ConsumerRecords
import lv.arvissk.governor.base.modules.sensors.SensorsProtocol.TimestampedReading

object KafkaReceiverProtocol {

  def props: Props = Props[KafkaReceiver]

}

class KafkaReceiver extends Actor {

  val extractor = ConsumerRecords.extractor[String, TimestampedReading]

  override def receive: Receive = {

    case extractor(records) =>

      processRecords(records.pairs)
      sender ! Confirm(records.offsets)
  }

  def processRecords(records: Seq[(Option[String], TimestampedReading)]) = {
    println(records)
  }

}