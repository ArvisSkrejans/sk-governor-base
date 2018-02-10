/*
* Sets up the logging subsystem that is able to utilize and log various sensors depending on the config.
*/
package lv.arvissk.governor.base.modules.logging

import akka.actor._
import lv.arvissk.governor.base.modules.ModuleHandler.ReportStartupSuccessful

object LoggingHandler {

  def props: Props = Props[LoggingHandler]

  final case object InitLogging

  final case object ShutdownLogging

}

class LoggingHandler() extends Actor {

  import LoggingHandler._

  def receive = {
    case InitLogging =>
      context.parent ! ReportStartupSuccessful("logging")

    case ShutdownLogging =>

  }

}
