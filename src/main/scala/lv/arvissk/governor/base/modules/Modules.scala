/*
* Actor that provides useful app console info.
*/
package lv.arvissk.governor.base.modules

import akka.actor.{Actor, ActorRef, Props}
import lv.arvissk.governor.base.console.output.Printer.PrintDecoratedEventToConsole

object Modules {

  def props(printerActor: ActorRef): Props = Props(new Modules(printerActor))

  final case object Start

  final case object Stop

}

class Modules(printerActor: ActorRef) extends Actor {

  import Modules._

  val allModules = List("sensors", "logging", "processing")

  def receive = {
    case Start =>
      for (module <- allModules) {
        printerActor ! PrintDecoratedEventToConsole("Module: \"" + module + "\" started")
      }

    case Stop =>
      for (module <- allModules) {
        printerActor ! PrintDecoratedEventToConsole("Module: \"" + module + "\" stopped")
      }
  }
}
