package lv.arvissk.governor.base.modules.sensors

import akka.actor._
import akka.stream.{OverflowStrategy, DelayOverflowStrategy, ActorMaterializer, ThrottleMode}
import akka.stream.scaladsl._
import scala.concurrent._
import scala.util.Random
import scala.concurrent.duration._
import lv.arvissk.governor.base.modules.sensors.SensorsHandler.SensorInitSuccessful

object DummySensor {

  def props(sensorName: String): Props = Props(new DummySensor(sensorName: String))

  final case object SetupSensor

  final case object StreamData

  case class TimestampedReading(id: Integer, processingTimestamp: Long)

}

class DummySensor(sensorName: String) extends Actor {

  import DummySensor._

  def receive = {
    case SetupSensor =>
      //TODO: set up sensor parameters, check sensor status.
      sender ! SensorInitSuccessful(sensorName)
    case StreamData =>
      receiveStream()
  }

  def sensorSink =
    Flow[TimestampedReading]
      .buffer(1, OverflowStrategy.dropBuffer)
      .delay(1 seconds, DelayOverflowStrategy.backpressure)
      .to(Sink.foreach(e => println(e)))

  def randomIntSource =
    Source.fromIterator(() => Iterator.continually(Random.nextInt(35)))

  def throttlingFlow = Flow[Int].throttle(
    elements = 1,
    per = 1.second,
    maximumBurst = 0,
    mode = ThrottleMode.Shaping
  )

  def enrichWithTimestamp =
    Flow[Int]
      .map { e =>
        TimestampedReading(e, System.currentTimeMillis())
      }

  def receiveStream(): Unit = {
    implicit val materializer = ActorMaterializer()

    randomIntSource
      .via(throttlingFlow)
      .via(enrichWithTimestamp)
      .to(sensorSink)
      .run()
  }

}