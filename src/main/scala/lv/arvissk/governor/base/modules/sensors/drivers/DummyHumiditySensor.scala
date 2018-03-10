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

class DummyHumiditySensor(sensorName: String) extends GenericSensor {

  import SensorsProtocol._

  override def receive = {
    case InitSensor =>
      sender ! SensorInitSuccessful(sensorName)
      pushDataUpstream(sender)
  }

  override def sensorDataSource =
    Source.fromIterator(() => Iterator.continually(Random.nextInt(35)))

  override def processStream =
    Flow[Int]
      .map { e =>
        TimestampedReading("humidity", e, System.currentTimeMillis(), sensorName)
      }

}