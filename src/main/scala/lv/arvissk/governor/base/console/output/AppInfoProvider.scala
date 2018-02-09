/*
* Actor that provides useful app console info.
*/
package lv.arvissk.governor.base.console.output

import akka.actor.{Actor, ActorRef, Props}
import lv.arvissk.governor.base.console.output.Printer.{PrintDecoratedEventToConsole, PrintToConsole}

object AppInfoProvider {

  def props(printerActor: ActorRef): Props = Props(new AppInfoProvider(printerActor))

  final case object GiveInitialWelcome

  final case object NotifyModuleInit

  final case object NotifyModuleShutdown

  final case object GiveInitShutdownMessage

  final case object GiveCompleteShutdownMessage

}

class AppInfoProvider(printerActor: ActorRef) extends Actor {

  import AppInfoProvider._

  val systemMessages: Map[String, String] = Map(
    "initialWelcome" -> "SK-Governor 0.1 started",
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
