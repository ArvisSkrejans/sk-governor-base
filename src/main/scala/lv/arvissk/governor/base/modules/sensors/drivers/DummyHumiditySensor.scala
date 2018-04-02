/**
  * Dummy humidity like sensor reading stream for testing
  **/
package lv.arvissk.governor.base.modules.sensors.drivers

import akka.actor.Props
import akka.stream._
import akka.stream.scaladsl._
import scala.concurrent._
import scala.concurrent.duration._
import scala.util.Random
import lv.arvissk.governor.base.modules.sensors.SensorsProtocol

object DummyHumiditySensor {

  def props(sensorName: String): Props = Props(new DummyHumiditySensor(sensorName: String))

}

class DummyHumiditySensor(sensorName: String) extends GenericSensor(sensorName: String) {

  import SensorsProtocol._

  override val readingType = "humidity"

  override def receive = {
    case InitSensor =>
      sender ! SensorInitSuccessful(sensorName)
      pushDataUpstream(sender)
  }

  override def sensorDataSource =
    Source.fromIterator(() => Iterator.continually(Random.nextInt(35)))

}