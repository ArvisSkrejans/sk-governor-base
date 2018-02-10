/*
* Sets up various processing tasks that can process incoming sensor data and store it accordingly.
*/
package lv.arvissk.governor.base.modules.processing

import akka.actor._
import lv.arvissk.governor.base.modules.ModuleHandler.ReportStartupSuccessful

object ProcessingHandler {

  def props: Props = Props[ProcessingHandler]

  final case object InitProcessing

  final case object ShutdownProcessing

}

class ProcessingHandler() extends Actor {

  import ProcessingHandler._

  def receive = {
    case InitProcessing =>
      context.parent ! ReportStartupSuccessful("processing")

    case ShutdownProcessing =>

  }

}
