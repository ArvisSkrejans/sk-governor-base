/**
* Sets up various processing tasks that can process incoming sensor data and store it accordingly.
*/
package lv.arvissk.governor.base.modules.processing

import akka.actor._
import org.apache.kafka.common.serialization._
import cakesolutions.kafka.akka._
import cakesolutions.kafka.akka.KafkaConsumerActor._
import com.typesafe.config._
import lv.arvissk.governor.base.modules.ModuleProtocol.ModuleStartupSuccessCallback
import lv.arvissk.governor.base.modules.logging.JsonDeserializer
import lv.arvissk.governor.base.modules.sensors.SensorsProtocol.TimestampedReading
import lv.arvissk.governor.base.modules.logging.LoggingProtocol._

object ProcessingProtocol {

  def props: Props = Props[ProcessingHandler]

  case object InitProcessing

  case object ShutdownProcessing

  case object ProcessSensorReadings

}

class ProcessingHandler() extends Actor {

  import ProcessingProtocol._

  val kafkaConsumerActor: ActorRef = initKafkaConsumerActor

  def receive = {
    case InitProcessing =>
      context.parent ! ModuleStartupSuccessCallback("processing")

    case ShutdownProcessing =>

    case ProcessSensorReadings =>

      kafkaConsumerActor ! Subscribe.AutoPartition(List("sensorReadings"))


  }

  def initKafkaConsumerActor: ActorRef = {

    val receiverActor: ActorRef = context.actorOf(KafkaReceiverProtocol.props, "kafkaSensorDataReceiver")

    val conf = ConfigFactory.load().getConfig("app.kafka")

    context.actorOf(
      KafkaConsumerActor.props(conf, new StringDeserializer(), new JsonDeserializer[TimestampedReading], receiverActor),
      "KafkaSensorDataConsumer")

  }

}
