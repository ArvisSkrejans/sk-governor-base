/** Sensor handling actor.
  *
  * Initializes various configured sensor inputs as an actor group that are accessible where needed.
  * Loads sensors from config.
  */
package lv.arvissk.governor.base.modules.sensors

import akka.actor.{Actor, ActorRef, Props}
import akka.util.Timeout
import scala.util.{Success, Failure}
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import com.typesafe.config.{Config, ConfigFactory}
import pureconfig._
import pureconfig.error.ConfigReaderFailures
import lv.arvissk.governor.base.console.output.PrinterProtocol.PrintDecoratedEventToConsole
import lv.arvissk.governor.base.modules.ModuleProtocol._
import lv.arvissk.governor.base.modules.logging.LoggingProtocol._
import lv.arvissk.governor.base.modules.sensors.drivers._

object SensorsProtocol {

  def props(printerActor: ActorRef): Props = Props(new SensorsHandler(printerActor))

  case object InitSensors

  case object ShutdownSensors

  case class SensorInitSuccessful(sensorName: String)

  case class SensorInitFailed(sensorName: String)

  case object StreamData

  case class PushInitializedSensorDataToLog(reading: TimestampedReading)

  case object InitSensor

  case class TimestampedReading(name: String, value: Int, timestamp: Long, sensorName: String)

  case class Areas(areas: Map[String, Area])

  case class Area(
                   areaName: String,
                   temperature: List[Map[String, String]],
                   humidity: List[Map[String, String]]
                 )

}

class SensorsHandler(printerActor: ActorRef) extends Actor {

  import SensorsProtocol._

  def receive = {
    case InitSensors =>

      getConfiguredAreas match {
        case Right(areaList) =>

          for ((areaKey: String, area: Area) <- areaList.areas) {

            for (sensorData <- area.temperature ++ area.humidity) {
              loadSensorByDriver(sensorData, areaKey)
            }
          }

        case Left(e) => throw new Exception("Failed loading area config!" + e.toString)
      }

      context.parent ! ModuleStartupSuccessCallback("sensors")

    case SensorInitSuccessful(sensorName: String) =>
      printerActor ! PrintDecoratedEventToConsole("\"" + sensorName + "\" initialized.")

    case SensorInitFailed(sensorName: String) =>
      printerActor ! PrintDecoratedEventToConsole("\"" + sensorName + "\" init failed.")

    case ShutdownSensors =>
    //TODO: Implement sensor shutdown logic

    case PushInitializedSensorDataToLog(reading: TimestampedReading) =>

      implicit val timeout: Timeout = Timeout(5 seconds)
      context.actorSelection("/user/moduleHandler/Logging").resolveOne().onComplete {
        case Success(loggingActor) =>
          loggingActor ! LogTimestampedSensorReading(reading)
        case Failure(error) => println(error)
      }
  }

  /**
    * Loads config data of areas and sensors
    */
  def getConfiguredAreas: Either[ConfigReaderFailures, Areas] = {

    val config: Config = ConfigFactory.load()
    loadConfig[Areas](config, "app.sensors")
  }

  /**
    * Loads sensor by it's defined driver
    */
  def loadSensorByDriver(sensor: Map[String, String], areaKey: String) = {

    val sensorId = areaKey + sensor("sensor-id").capitalize

    sensor("sensor-driver") match {
      case "dummyTemperatureSensor" =>
        val dummyTemperatureSensor: ActorRef = context.actorOf(DummyTemperatureSensor.props(sensorId), sensorId)
        dummyTemperatureSensor ! InitSensor
        printerActor ! PrintDecoratedEventToConsole("Sensor: \"" + sensorId + "\" initializing..")
      case "dummyHumiditySensor" =>
        val dummyHumiditySensor: ActorRef = context.actorOf(DummyHumiditySensor.props(sensorId), sensorId)
        dummyHumiditySensor ! InitSensor
        printerActor ! PrintDecoratedEventToConsole("Sensor: \"" + sensorId + "\" initializing..")
      case _ => printerActor ! PrintDecoratedEventToConsole("Sensor: \"" + sensorId + "\" not loaded. Driver not supported!")
    }
  }
}
