/**
  * Sets up the logging subsystem that is able to utilize and log various sensors depending on the config.
  */
package lv.arvissk.governor.base.modules.logging

import akka.actor._
import com.typesafe.config.ConfigFactory
import cakesolutions.kafka.akka.{KafkaProducerActor, ProducerRecords}
import cakesolutions.kafka.{KafkaConsumer, KafkaProducer, KafkaProducerRecord}
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import lv.arvissk.governor.base.modules.ModuleProtocol.ModuleStartupSuccessCallback

class LoggingHandler() extends Actor {

  import LoggingProtocol._
  import lv.arvissk.governor.base.modules.sensors.SensorsHandler._

  def receive = {
    case InitLogging =>
      context.parent ! ModuleStartupSuccessCallback("logging")

    case ShutdownLogging =>
    //TODO: implement clean login shutdown

    case LogStream(streamProviderActor: ActorRef) =>
      val kafkaProducerActor = initKafkaProducerActor
      streamProviderActor ! StreamData

  }

  def initKafkaProducerActor: ActorRef =
  {
    val kafkaProducerConf = KafkaProducer.Conf(
      bootstrapServers = ConfigFactory.load().getString("app.kafka.bootstrap.servers"),
      keySerializer = new StringSerializer(),
      valueSerializer = new JsonSerializer[LogMessage])

     context.actorOf(KafkaProducerActor.props( kafkaProducerConf))
  }

}
