/*
* Initializes various configured sensor inputs as an actor group that are accessible where needed.
*/
package lv.arvissk.governor.base.modules.sensors

import akka.actor._
import lv.arvissk.governor.base.modules.ModuleHandler.ReportStartupSuccessful

object Sensors {

  def props: Props = Props[Sensors]

  final case object InitSensors

  final case object ShutdownSensors

}

class Sensors() extends Actor {

  import Sensors._

  def receive = {
    case InitSensors =>
      context.parent ! ReportStartupSuccessful("sensors")

    case ShutdownSensors =>

  }

}
