/*
* Actor that provides useful app console info.
*/
package lv.arvissk.governor.base.modules

import akka.actor._
import scala.language.postfixOps
import lv.arvissk.governor.base.console.output.Printer.PrintDecoratedEventToConsole
import lv.arvissk.governor.base.modules.sensors.SensorsHandler
import lv.arvissk.governor.base.modules.logging.LoggingHandler
import lv.arvissk.governor.base.modules.processing.ProcessingHandler

object ModuleHandler {

  def props(printerActor: ActorRef): Props = Props(new ModuleHandler(printerActor))

  final case object InitAllModules

  final case class ReportStartupSuccessful(module: String)

  final case class ReportStartupFailed(module: String)

  final case object ShutdownAllModules

}

class ModuleHandler(printerActor: ActorRef) extends Actor {

  import ModuleHandler._
  import SensorsHandler._
  import LoggingHandler._
  import ProcessingHandler._

  val allModules = List("sensors", "logging", "processing")

  def receive = {
    case InitAllModules =>
      for (moduleName <- allModules) {
        moduleName match {
          case "sensors" =>
            val sensorsActor: ActorRef = context.actorOf(SensorsHandler.props(printerActor), "Sensors")
            sensorsActor ! InitSensors
          case "logging" =>
            val loggingActor: ActorRef = context.actorOf(Props[LoggingHandler], "Logging")
            loggingActor ! InitLogging
          case "processing" =>
            val processingActor: ActorRef = context.actorOf(Props[ProcessingHandler], "Processing")
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
