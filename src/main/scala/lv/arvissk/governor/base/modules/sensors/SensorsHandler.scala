/**
  * Initializes various configured sensor inputs as an actor group that are accessible where needed.
  **/
package lv.arvissk.governor.base.modules.sensors

import akka.actor._
import lv.arvissk.governor.base.console.output.Printer.PrintDecoratedEventToConsole
import lv.arvissk.governor.base.modules.ModuleHandler._

object SensorsHandler {

  def props(printerActor: ActorRef): Props = Props(new SensorsHandler(printerActor))

  final case object InitSensors

  final case object ShutdownSensors

  final case class SensorInitSuccessful(sensor: String)

  final case class SensorInitFailed(sensor: String)

  final case object StreamData

  final case object SetupSensor

  case class TimestampedReading(id: Integer, processingTimestamp: Long)

}

class SensorsHandler(printerActor: ActorRef) extends Actor {

  import SensorsHandler._

  //TODO: take enabled sensor list from config
  val enabledSensors = List("dummySensor")

  def receive = {
    case InitSensors =>
      for (sensorName <- enabledSensors) {

        //TODO: Implement sensor config init
        sensorName match {
          case "dummySensor" =>
            val dummySensor: ActorRef = context.actorOf(DummySensor.props("dummySensorTest"), "dummySensorTest")
            dummySensor ! SetupSensor
        }

      }
      context.parent ! ModuleStartupSuccessCallback("sensors")

    case ShutdownSensors =>
    //TODO: Implement sensor shutdown logic

    case SensorInitSuccessful(sensorName: String) =>

      printerActor ! PrintDecoratedEventToConsole("Sensor: \"" + sensorName + "\" initialized.")
      context.parent ! LogSensorData(sensorName)
  }

}
