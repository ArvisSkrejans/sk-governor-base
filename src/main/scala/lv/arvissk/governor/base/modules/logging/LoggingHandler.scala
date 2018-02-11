/**
  * Sets up the logging subsystem that is able to utilize and log various sensors depending on the config.
  */
package lv.arvissk.governor.base.modules.logging

import akka.actor._
import com.typesafe.config.ConfigFactory
import cakesolutions.kafka.akka.{KafkaProducerActor}
import cakesolutions.kafka.{KafkaConsumer, KafkaProducer, KafkaProducerRecord}
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import lv.arvissk.governor.base.modules.ModuleProtocol.ModuleStartupSuccessCallback
import lv.arvissk.governor.base.modules.sensors.SensorsProtocol._

class LoggingHandler() extends Actor {

  import LoggingProtocol._

  def receive = {
    case InitLogging =>
      context.parent ! ModuleStartupSuccessCallback("logging")

    case ShutdownLogging =>
    //TODO: implement clean login shutdown

    case LogTimestampedSensorReading(reading: TimestampedReading) =>
      val kafkaProducerActor = initKafkaProducerActor

      def uuid = java.util.UUID.randomUUID.toString

      kafkaProducerActor ! KafkaProducerRecord("sensorReadings", uuid, reading)

  }

  def initKafkaProducerActor: ActorRef = {
    val kafkaProducerConf = KafkaProducer.Conf(
      bootstrapServers = ConfigFactory.load().getString("app.kafka.bootstrap.servers"),
      keySerializer = new StringSerializer(),
      valueSerializer = new JsonSerializer[TimestampedReading])

    context.actorOf(KafkaProducerActor.props(kafkaProducerConf))
  }

}
