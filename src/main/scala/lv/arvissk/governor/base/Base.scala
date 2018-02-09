
package lv.arvissk.governor.base

import akka.actor.{ActorSystem, ActorRef}
import scala.concurrent.duration._
import scala.concurrent.Await
import lv.arvissk.governor.base.console.output.Printer
import lv.arvissk.governor.base.console.output.AppInfoProvider
import lv.arvissk.governor.base.console.output.AppInfoProvider._
import lv.arvissk.governor.base.modules.Modules
import lv.arvissk.governor.base.modules.Modules.{Start, Stop}

object Base extends App {

  //init main actor system
  val system: ActorSystem = ActorSystem("skGovernor")

  //init console console output actor
  val printer: ActorRef = system.actorOf(Printer.props, "printerActor")

  //init app status info handling actor
  val appInfoProvider: ActorRef =
    system.actorOf(AppInfoProvider.props(printer), "appInfoProviderActor")
  //init module handling actor
  val moduleHandler: ActorRef =
    system.actorOf(Modules.props(printer), "moduleHandlerActor")

  //start the application and all the modules
  appInfoProvider ! GiveInitialWelcome
  appInfoProvider ! NotifyModuleInit
  moduleHandler ! Start

  //handle graceful app and module shutdown
  scala.sys.addShutdownHook {

    appInfoProvider ! GiveInitShutdownMessage
    appInfoProvider ! NotifyModuleShutdown
    moduleHandler ! Stop
    appInfoProvider ! GiveCompleteShutdownMessage
    system.terminate()
    Await.result(system.whenTerminated, 30 seconds)
  }

}
