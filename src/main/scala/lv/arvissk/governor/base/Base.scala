
package lv.arvissk.governor.base

import akka.actor.{ActorSystem, ActorRef}
import scala.concurrent.duration._
import lv.arvissk.governor.base.console.output.Printer
import lv.arvissk.governor.base.console.output.AppInfoProvider
import lv.arvissk.governor.base.console.output.AppInfoProvider.{GiveInitialWelcome, GiveInitShutdownMessage, GiveCompleteShutdownMessage,  NotifyModuleInit}

import scala.concurrent.Await


object Base extends App {


  val system: ActorSystem = ActorSystem("skGovernor")
  val printer: ActorRef = system.actorOf(Printer.props, "printerActor")

  val appInfoProvider: ActorRef =
    system.actorOf(AppInfoProvider.props(printer), "appInfoProvider")


  appInfoProvider ! GiveInitialWelcome
  appInfoProvider ! NotifyModuleInit

  scala.sys.addShutdownHook {

    appInfoProvider ! GiveInitShutdownMessage
    appInfoProvider ! GiveCompleteShutdownMessage
    system.terminate()
    Await.result(system.whenTerminated, 30 seconds)
  }

}
