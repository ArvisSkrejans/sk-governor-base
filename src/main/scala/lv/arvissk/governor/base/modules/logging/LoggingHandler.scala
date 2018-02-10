/**
  * Sets up the logging subsystem that is able to utilize and log various sensors depending on the config.
  */
package lv.arvissk.governor.base.modules.logging

import akka.actor._
import lv.arvissk.governor.base.modules.ModuleHandler.ModuleStartupSuccessCallback

object LoggingHandler {

  def props: Props = Props[LoggingHandler]

  final case object InitLogging

  final case object ShutdownLogging

  final case class LogStream(streamProviderActor: ActorRef)

}

class LoggingHandler() extends Actor {

  import LoggingHandler._
  import lv.arvissk.governor.base.modules.sensors.SensorsHandler._

  def receive = {
    case InitLogging =>
      context.parent ! ModuleStartupSuccessCallback("logging")

    case ShutdownLogging =>
    //TODO: implement clean login shutdown

    case LogStream(streamProviderActor: ActorRef) =>
      streamProviderActor ! StreamData

  }

}
