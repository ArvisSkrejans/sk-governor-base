/**
  * Generic sensor base class
  **/
package lv.arvissk.governor.base.modules.sensors.drivers

import lv.arvissk.governor.base.modules.sensors.SensorsProtocol

import akka.actor._
import akka.stream._
import akka.stream.scaladsl._
import scala.concurrent._
import scala.concurrent.duration._
import lv.arvissk.governor.base.modules.sensors.SensorsProtocol

abstract class GenericSensor extends Actor {

  import SensorsProtocol._

  def receive = {
    case InitSensor =>
      sender ! SensorInitSuccessful("genericSensor")
      pushDataUpstream(sender)
  }

  def sensorPushSink(sender: ActorRef) =
    Flow[TimestampedReading]
      .buffer(1, OverflowStrategy.dropBuffer)
      .delay(1 seconds, DelayOverflowStrategy.backpressure)
      .to(Sink.foreach(e => sender ! PushInitializedSensorDataToLog(e)))

  def sensorDataSource =
    Source.single(1)

  def throttlingFlow = Flow[Int].throttle(
    elements = 1,
    per = 1.second,
    maximumBurst = 0,
    mode = ThrottleMode.Shaping
  )

  def processStream =
    Flow[Int]
      .map { e =>
        TimestampedReading("genericSensorReading", e, System.currentTimeMillis(), "genericSensor")
      }

  def pushDataUpstream(sender: ActorRef): Unit = {
    implicit val materializer: ActorMaterializer = ActorMaterializer()

    sensorDataSource
      .via(throttlingFlow)
      .via(processStream)
      .to(sensorPushSink(sender))
      .run()
  }

}