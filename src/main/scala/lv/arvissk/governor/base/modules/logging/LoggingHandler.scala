/**
  * Sets up the logging subsystem that is able to utilize and log various sensors depending on the config.
  */
package lv.arvissk.governor.base.modules.logging

import akka.actor._
import cakesolutions.kafka.akka._
import cakesolutions.kafka._
import com.typesafe.config._
import org.apache.kafka.common.serialization._
import lv.arvissk.governor.base.modules.ModuleProtocol.ModuleStartupSuccessCallback
import lv.arvissk.governor.base.modules.sensors.SensorsProtocol._


class LoggingHandler() extends Actor {

  import LoggingProtocol._

  val kafkaProducerActor: ActorRef = initKafkaProducerActor

  def receive = {
    case InitLogging =>
      context.parent ! ModuleStartupSuccessCallback("logging")

    case ShutdownLogging =>
    //TODO: implement clean login shutdown

    case LogTimestampedSensorReading(reading: TimestampedReading) =>

      def uuid = java.util.UUID.randomUUID.toString

      kafkaProducerActor ! ProducerRecords(List(KafkaProducerRecord("sensorReadings", uuid, reading)))
  }

  def initKafkaProducerActor: ActorRef = {
    val kafkaProducerConf = KafkaProducer.Conf(
      bootstrapServers = ConfigFactory.load().getString("app.kafka.bootstrap.servers"),
      keySerializer = new StringSerializer(),
      valueSerializer = new JsonSerializer[TimestampedReading])

    context.actorOf(KafkaProducerActor.props(kafkaProducerConf))
  }

}
