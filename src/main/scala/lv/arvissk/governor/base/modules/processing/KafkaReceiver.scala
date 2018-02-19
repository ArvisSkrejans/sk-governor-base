
package lv.arvissk.governor.base.modules.processing

import akka.actor._
import akka.util.Timeout
import scala.util.{Success, Failure}
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import cakesolutions.kafka.akka.KafkaConsumerActor.Confirm
import cakesolutions.kafka.akka.ConsumerRecords
import lv.arvissk.governor.base.modules.logging.LoggingProtocol.LogTimestampedSensorReading
import lv.arvissk.governor.base.modules.sensors.SensorsProtocol.TimestampedReading
import lv.arvissk.governor.base.modules.processing.ProcessingProtocol.saveTemperatureToElastic

import scala.util.{Failure, Success}

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

    implicit val timeout: Timeout = Timeout(5 seconds)

    context.actorSelection("/user/moduleHandler/Processing").resolveOne().onComplete {
      case Success(processingHandler) =>
        records.foreach (x => processingHandler ! saveTemperatureToElastic(x._2))
      case Failure(error) => println(error)
    }

  }

}