/*
* Sets up the logging subsystem that is able to utilize and log various sensors depending on the config.
*/
package lv.arvissk.governor.base.modules.logging

import akka.actor._
import lv.arvissk.governor.base.modules.ModuleHandler.ReportStartupSuccessful

object Logging {

  def props: Props = Props[Logging]

  final case object InitLogging

  final case object ShutdownLogging

}

class Logging() extends Actor {

  import Logging._

  def receive = {
    case InitLogging =>
      context.parent ! ReportStartupSuccessful("logging")

    case ShutdownLogging =>

  }

}
