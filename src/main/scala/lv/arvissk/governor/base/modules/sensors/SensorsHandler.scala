/*
* Initializes various configured sensor inputs as an actor group that are accessible where needed.
*/
package lv.arvissk.governor.base.modules.sensors

import akka.actor._
import scala.concurrent.duration._
import akka.util.Timeout
import scala.util.{Success, Failure}
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import lv.arvissk.governor.base.console.output.Printer.PrintDecoratedEventToConsole
import lv.arvissk.governor.base.modules.ModuleHandler.ReportStartupSuccessful

object SensorsHandler {

  def props(printerActor: ActorRef): Props = Props(new SensorsHandler(printerActor))

  final case object InitSensors

  final case object ShutdownSensors

  final case class SensorInitSuccessful(sensor: String)

  final case class SensorInitFailed(sensor: String)

}

class SensorsHandler(printerActor: ActorRef) extends Actor {

  import SensorsHandler._
  import DummySensor._

  val enabledSensors = List("dummySensor")

  def receive = {
    case InitSensors =>
      for (sensorName <- enabledSensors) {

        sensorName match {
          case "dummySensor" =>
            val dummySensor: ActorRef = context.actorOf(DummySensor.props("dummySensorTest"), "dummySensorTest")
            dummySensor ! SetupSensor
        }

      }
      context.parent ! ReportStartupSuccessful("sensors")

    case ShutdownSensors =>

    case SensorInitSuccessful(sensorName: String) =>

      printerActor ! PrintDecoratedEventToConsole("Sensor:" + sensorName + " initialized.")

      implicit val timeout = Timeout(5 seconds)
        context.system.actorSelection("/user/moduleHandler/Sensors/" + sensorName).resolveOne().onComplete {
        case Success(sensorActor) =>
          sensorActor ! StreamData
        case Failure(error) => println(error)
      }

  }

}
