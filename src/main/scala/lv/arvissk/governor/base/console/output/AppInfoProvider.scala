/**
  * Actor that provides useful app console info.
  */
package lv.arvissk.governor.base.console.output

import akka.actor.{Actor, ActorRef, Props}
import lv.arvissk.governor.base.console.output.PrinterProtocol.{PrintDecoratedEventToConsole, PrintToConsole}
import com.typesafe.config.ConfigFactory

object AppInfoProtocol {

  def props(printerActor: ActorRef): Props = Props(new AppInfoProvider(printerActor))

  case object GiveInitialWelcome

  case object NotifyModuleInit

  case object NotifyModuleShutdown

  case object GiveInitShutdownMessage

  case object GiveCompleteShutdownMessage

}

class AppInfoProvider(printerActor: ActorRef) extends Actor {

  import AppInfoProtocol._

  def appVersion: String = {
    ConfigFactory.load().getString("app.stats.version")
  }

  val systemMessages: Map[String, String] = Map(
    "initialWelcome" -> "SK-Governor ".concat(appVersion).concat(" started"),
    "shutdownInitMessage" -> "SK-Governor shut down started..",
    "shutdownCompleteMessage" -> "SK-Governor shut down complete.",
    "moduleInit" -> "Initializing modules..",
    "moduleShutdown" -> "Stopping modules.."
  )

  def receive = {
    case GiveInitialWelcome =>
      printerActor ! PrintToConsole(systemMessages("initialWelcome"))
    case NotifyModuleInit =>
      printerActor ! PrintDecoratedEventToConsole(systemMessages("moduleInit"))
    case NotifyModuleShutdown =>
      printerActor ! PrintDecoratedEventToConsole(systemMessages("moduleShutdown"))
    case GiveInitShutdownMessage =>
      printerActor ! PrintDecoratedEventToConsole(systemMessages("shutdownInitMessage"))
    case GiveCompleteShutdownMessage =>
      printerActor ! PrintToConsole(systemMessages("shutdownCompleteMessage"))
  }
}
