/*
* Actor that provides useful app console info.
*/
package lv.arvissk.governor.base.modules

import akka.actor._
import scala.language.postfixOps
import lv.arvissk.governor.base.console.output.Printer.PrintDecoratedEventToConsole
import lv.arvissk.governor.base.modules.sensors.Sensors
import lv.arvissk.governor.base.modules.logging.Logging
import lv.arvissk.governor.base.modules.processing.Processing

object ModuleHandler {

  def props(printerActor: ActorRef): Props = Props(new ModuleHandler(printerActor))

  final case object InitAllModules

  final case class ReportStartupSuccessful(module: String)

  final case class ReportStartupFailed(module: String)

  final case object ShutdownAllModules

}

class ModuleHandler(printerActor: ActorRef) extends Actor {

  import ModuleHandler._
  import Sensors._
  import Logging._
  import Processing._

  val allModules = List("sensors", "logging", "processing")

  def receive = {
    case InitAllModules =>
      for (moduleName <- allModules) {
        moduleName match {
          case "sensors" =>
            val sensorsActor: ActorRef = context.actorOf(Props[Sensors], "SensorsActor")
            sensorsActor ! InitSensors
          case "logging" =>
            val loggingActor: ActorRef = context.actorOf(Props[Logging], "LoggingActor")
            loggingActor ! InitLogging
          case "processing" =>
            val processingActor: ActorRef = context.actorOf(Props[Processing], "ProcessingActor")
            processingActor ! InitProcessing
        }
      }

    case ReportStartupSuccessful(moduleName: String) =>
      printerActor ! PrintDecoratedEventToConsole("Module: \"" + moduleName + "\" started")

    case ReportStartupFailed(moduleName: String) =>
      printerActor ! PrintDecoratedEventToConsole("Module: \"" + moduleName + "\" init failed")

    case ShutdownAllModules =>
      for (moduleName <- allModules) {
        printerActor ! PrintDecoratedEventToConsole("Module: \"" + moduleName + "\" stopped")
      }
  }
}
