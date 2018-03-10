/**
  * Application base class that manages actor system and app logic flow and init.
  **/
package lv.arvissk.governor.base

import akka.actor._
import scala.concurrent.duration._
import scala.concurrent.Await
import lv.arvissk.governor.base.modules._
import lv.arvissk.governor.base.console.output._

object Base extends App {

  import ModuleProtocol._
  import AppInfoProtocol._

  //init main actor system
  val system: ActorSystem = ActorSystem("skGovernor")

  //init console console output actor
  val printer: ActorRef = system.actorOf(PrinterProtocol.props, "printer")

  //init app status info handling actor
  val appInfoProvider: ActorRef =
    system.actorOf(AppInfoProtocol.props(printer), "appInfoProvider")
  //init module handling actor
  val moduleHandler: ActorRef =
    system.actorOf(ModuleProtocol.props(printer), "moduleHandler")

  //start the application and all the modules
  appInfoProvider ! GiveInitialWelcome
  appInfoProvider ! NotifyModuleInit
  moduleHandler ! InitAllModules

  //handle graceful app and module shutdown
  scala.sys.addShutdownHook {

    appInfoProvider ! GiveInitShutdownMessage
    appInfoProvider ! NotifyModuleShutdown
    moduleHandler ! ShutdownAllModules
    appInfoProvider ! GiveCompleteShutdownMessage

    moduleHandler ! PoisonPill
    appInfoProvider ! PoisonPill
    printer ! PoisonPill
    system.terminate()
    Await.result(system.whenTerminated, 30 seconds)
  }

}
