/**
  * Initializes various configured sensor inputs as an actor group that are accessible where needed.
  **/
package lv.arvissk.governor.base.modules.sensors

import akka.actor._
import akka.util.Timeout
import scala.util.{Success, Failure}
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import lv.arvissk.governor.base.console.output.PrinterProtocol.PrintDecoratedEventToConsole
import lv.arvissk.governor.base.modules.ModuleProtocol._
import lv.arvissk.governor.base.modules.logging.LoggingProtocol._

import scala.util.{Failure, Success}

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

}

class SensorsHandler(printerActor: ActorRef) extends Actor {

  import SensorsProtocol._

  //TODO: take enabled sensor list from config
  val enabledSensors = List("dummySensor")

  def receive = {
    case InitSensors =>
      for (sensorName <- enabledSensors) {

        //TODO: Implement sensor config init
        sensorName match {
          case "dummySensor" =>
            val dummySensor: ActorRef = context.actorOf(DummySensor.props("dummySensorTest"), "dummySensorTest")
            dummySensor ! InitSensor
            printerActor ! PrintDecoratedEventToConsole("Sensor: \"dummySensorTest\" initializing..")
        }

      }
      context.parent ! ModuleStartupSuccessCallback("sensors")

    case SensorInitSuccessful(sensorName: String) =>
      printerActor ! PrintDecoratedEventToConsole("Sensor: \"" + sensorName + "\" initialized.")

    case SensorInitFailed(sensorName: String) =>
      printerActor ! PrintDecoratedEventToConsole("Sensor: \"" + sensorName + "\" init failed.")

    case ShutdownSensors =>
    //TODO: Implement sensor shutdown logic

    case PushInitializedSensorDataToLog(reading: TimestampedReading) =>

      implicit val timeout = Timeout(5 seconds)
      context.actorSelection("/user/moduleHandler/Logging").resolveOne().onComplete {
        case Success(loggingActor) =>
          loggingActor ! LogTimestampedSensorReading(reading)
        case Failure(error) => println(error)
      }
  }
}
