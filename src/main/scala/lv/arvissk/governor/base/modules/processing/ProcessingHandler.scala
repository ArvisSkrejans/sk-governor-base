/**
  * Sets up various processing tasks that can process incoming sensor data and store it accordingly.
  */
package lv.arvissk.governor.base.modules.processing

import akka.actor._
import org.apache.kafka.common.serialization._
import cakesolutions.kafka.akka._
import cakesolutions.kafka.akka.KafkaConsumerActor._
import com.typesafe.config._
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.index.admin._
import com.sksamuel.elastic4s.http.{HttpClient, RequestFailure, RequestSuccess}
import com.sksamuel.elastic4s.ElasticsearchClientUri
import com.sksamuel.elastic4s.RefreshPolicy
import com.github.nscala_time.time.Imports._
import org.apache.kafka.common.serialization.{Deserializer, Serializer, StringDeserializer, StringSerializer}
import lv.arvissk.governor.base.modules.ModuleProtocol.ModuleStartupSuccessCallback
import lv.arvissk.governor.base.modules.logging.JsonDeserializer
import lv.arvissk.governor.base.modules.sensors.SensorsProtocol.TimestampedReading
import lv.arvissk.governor.base.modules.logging.LoggingProtocol._

object ProcessingProtocol {

  def props: Props = Props[ProcessingHandler]

  case object InitProcessing

  case object ShutdownProcessing

  case object ProcessSensorReadings

  case class saveTemperatureToElastic(reading: TimestampedReading)

}

class ProcessingHandler() extends Actor {

  import ProcessingProtocol._

  val currentDayTimestamp: String = {
    val dateFormat = DateTimeFormat.forPattern("yyy-MM-dd")
    dateFormat.print(DateTime.now())
  }

  val kafkaConsumerActor: ActorRef = initKafkaConsumerActor
  val client = HttpClient(ElasticsearchClientUri("localhost", 9200))

  def receive = {
    case InitProcessing =>
      context.parent ! ModuleStartupSuccessCallback("processing")

    case ShutdownProcessing =>

    case ProcessSensorReadings =>

      kafkaConsumerActor ! Subscribe.AutoPartition(List("sensorReadings"))

    case saveTemperatureToElastic(reading: TimestampedReading) =>

      val indexName = reading.name + "-log-" + this.currentDayTimestamp

      client.execute {
        createIndex(indexName)
      }

      client.execute {
        indexInto(indexName / reading.sensorName)
          .fields(reading.name -> reading.value, "timestamp" -> reading.timestamp)
          .refresh(RefreshPolicy.IMMEDIATE)
      }
  }

  def initKafkaConsumerActor: ActorRef = {

    val receiverActor: ActorRef = context.actorOf(KafkaReceiverProtocol.props, "kafkaSensorDataReceiver")

    val conf = ConfigFactory.load().getConfig("app.kafka")

    context.actorOf(
      KafkaConsumerActor.props(conf, new StringDeserializer(), new JsonDeserializer[TimestampedReading], receiverActor),
      "KafkaSensorDataConsumer")

  }

}
