/*
* Actor that prints to console and logs when needed.
*/

package lv.arvissk.governor.base.console.output

import akka.actor.{Actor, ActorLogging, Props}


object Printer {

  def props: Props = Props[Printer]

  final case class PrintToConsole(data: String)
  final case class PrintDecoratedEventToConsole(data: String)

}

class Printer extends Actor with ActorLogging {

  import Printer._

  val eventDecorator: String = "[*] "

  def receive = {
    case PrintToConsole(data) =>
      println(data)
    case PrintDecoratedEventToConsole(data) =>
      println(eventDecorator + data)
  }
}