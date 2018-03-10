/**
  * Actor that prints to console and logs when needed.
  */
package lv.arvissk.governor.base.console.output

import akka.actor.{Props, Actor, ActorLogging}


object PrinterProtocol {

  def props: Props = Props[Printer]

  case class PrintToConsole(data: String)
  case class PrintDecoratedEventToConsole(data: String)

}

class Printer extends Actor with ActorLogging {

  import PrinterProtocol._

  val eventDecorator: String = "[*] "

  def receive = {
    case PrintToConsole(data) =>
      println(data)
    case PrintDecoratedEventToConsole(data) =>
      println(eventDecorator + data)
  }
}