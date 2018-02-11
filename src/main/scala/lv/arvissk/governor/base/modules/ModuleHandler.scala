/**
  * Actor that provides useful app console info.
  **/
package lv.arvissk.governor.base.modules

import akka.actor._
import scala.language.postfixOps
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
  import SensorsProtocol._
  import LoggingProtocol._
  import ProcessingProtocol._

  val allModules = List("sensors", "logging", "processing")

  def receive = {

    case InitAllModules =>

      val processingActor: ActorRef = context.actorOf(Props[ProcessingHandler], "Processing")
      processingActor ! InitProcessing

    case ModuleStartupSuccessCallback(moduleName: String) =>
      printerActor ! PrintDecoratedEventToConsole("Module: \"" + moduleName + "\" initialized")
      moduleName match {
        case "processing" =>
          val loggingActor: ActorRef = context.actorOf(Props[LoggingHandler], "Logging")
          loggingActor ! InitLogging

        case "logging" =>
          val sensorsActor: ActorRef = context.actorOf(SensorsProtocol.props(printerActor), "Sensors")
          sensorsActor ! InitSensors
        case _ =>
      }

    case ModuleStartupFailureCallback(moduleName: String) =>
      printerActor ! PrintDecoratedEventToConsole("Module: \"" + moduleName + "\" init failed")

    case ShutdownAllModules =>
      for (moduleName <- allModules) {
        printerActor ! PrintDecoratedEventToConsole("Module: \"" + moduleName + "\" stopped")
      }
  }
}
