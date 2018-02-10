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
import lv.arvissk.governor.base.console.output.Printer.PrintDecoratedEventToConsole
import lv.arvissk.governor.base.modules.sensors._
import lv.arvissk.governor.base.modules.logging.LoggingHandler
import lv.arvissk.governor.base.modules.processing.ProcessingHandler


object ModuleHandler {

  def props(printerActor: ActorRef): Props = Props(new ModuleHandler(printerActor))

  final case object InitAllModules

  final case object ShutdownAllModules

  final case class ModuleStartupSuccessCallback(module: String)

  final case class ModuleStartupFailureCallback(module: String)

  final case class LogSensorData(sensorName: String)

}

class ModuleHandler(printerActor: ActorRef) extends Actor {

  import ModuleHandler._
  import SensorsHandler._
  import LoggingHandler._
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
