/*
* Sets up various processing tasks that can process incoming sensor data and store it accordingly.
*/
package lv.arvissk.governor.base.modules.processing

import akka.actor._
import lv.arvissk.governor.base.modules.ModuleHandler.ReportStartupSuccessful

object Processing {

  def props: Props = Props[Processing]

  final case object InitProcessing

  final case object ShutdownProcessing

}

class Processing() extends Actor {

  import Processing._

  def receive = {
    case InitProcessing =>
      context.parent ! ReportStartupSuccessful("processing")

    case ShutdownProcessing =>

  }

}
