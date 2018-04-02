/**
  * Reads HTTP request from ESP-01 run web server, that collects DHT-11 sensor humidity data.
  **/
package lv.arvissk.governor.base.modules.sensors.drivers

import akka.actor.{ActorRef, Props}
import akka.http.scaladsl.Http
import akka.stream.scaladsl.Flow
import net.liftweb.json._
import scala.concurrent.ExecutionContext.Implicits.global

object Esp01Dht11HttpHumiditySensor {

  def props(sensorName: String, sensorIp: String, sensorPort: String, readDuration: Int, readDelay: Int): Props = Props(new Esp01Dht11HttpHumiditySensor(sensorName: String, sensorIp: String, sensorPort: String, readDuration: Int, readDelay: Int))

}

case class Dht11HumidityResponse(humidity: String)

class Esp01Dht11HttpHumiditySensor(sensorName: String, sensorIp: String, sensorPort: String, readDuration: Int, readDelay: Int) extends Esp01Dht11HttpSensorBase(sensorName: String, readDuration: Int, readDelay: Int) {

  override val readingType = "humidity"
  lazy val pool = Http().newHostConnectionPool[Int](sensorIp, sensorPort.toInt)

  override def parseDefaultReading = {
    parse("{\"humidity\" : \"0\"}")
  }

  def extractReading =
    Flow[JValue]
      .map { e: JValue =>
        implicit val formats = net.liftweb.json.DefaultFormats
        val response = e.extract[Dht11HumidityResponse]
        response.humidity.toInt
      }

  override def pushDataUpstream(sender: ActorRef): Unit = {

    sensorDataSourceLocal.get("/humidity", pool)
      .via(extractReading)
      .via(processStream)
      .to(sensorPushSink(sender))
      .run()
  }

}