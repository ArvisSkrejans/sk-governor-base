/**
  * Actor that provides useful app console info.
  **/
package lv.arvissk.governor.base.modules

import akka.actor._
import akka.util.Timeout
import scala.language.postfixOps
import scala.util.{Success, Failure}
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import lv.arvissk.governor.base.console.output.PrinterProtocol.PrintDecoratedEventToConsole
import lv.arvissk.governor.base.modules.sensors._
import lv.arvissk.governor.base.modules.logging._
import lv.arvissk.governor.base.modules.processing._


object ModuleProtocol {

  def props(printerActor: ActorRef): Props = Props(new ModuleHandler(printerActor))

  case object InitAllModules

  case object ShutdownAllModules

  case class ModuleStartupSuccessCallback(module: String)

  case class ModuleStartupFailureCallback(module: String)

  case class LogSensorData(sensorName: String)

}

class ModuleHandler(printerActor: ActorRef) extends Actor {

  import ModuleProtocol._
  import SensorsHandler._
  import LoggingProtocol._
  import ProcessingHandler._

  val allModules = List("sensors", "logging", "processing")

  def receive = {

    case InitAllModules =>

      val loggingActor: ActorRef = context.actorOf(Props[LoggingHandler], "Logging")
      loggingActor ! InitLogging

      val processingActor: ActorRef = context.actorOf(Props[ProcessingHandler], "Processing")
      processingActor ! InitProcessing

    case ModuleStartupSuccessCallback(moduleName: String) =>
      printerActor ! PrintDecoratedEventToConsole("Module: \"" + moduleName + "\" started")
      moduleName match {
        case "processing" =>
          val sensorsActor: ActorRef = context.actorOf(SensorsHandler.props(printerActor), "Sensors")
          sensorsActor ! InitSensors
        case _ =>
      }

    case ModuleStartupFailureCallback(moduleName: String) =>
      printerActor ! PrintDecoratedEventToConsole("Module: \"" + moduleName + "\" init failed")

    case ShutdownAllModules =>
      for (moduleName <- allModules) {
        printerActor ! PrintDecoratedEventToConsole("Module: \"" + moduleName + "\" stopped")
      }
    case LogSensorData(sensorName: String) =>
      implicit val timeout = Timeout(5 seconds)
      context.system.actorSelection("/user/moduleHandler/Sensors/" + sensorName).resolveOne().onComplete {
        case Success(sensorActor) =>
          context.actorSelection("/user/moduleHandler/Logging") ! LogStream(sensorActor)
        case Failure(error) => println(error)
      }

  }
}
