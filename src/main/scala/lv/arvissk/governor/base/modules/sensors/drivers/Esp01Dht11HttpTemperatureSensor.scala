/**
  * Reads HTTP request from ESP-01 run web server, that collects DHT-11 sensor temperature data.
  **/
package lv.arvissk.governor.base.modules.sensors.drivers

import akka.actor.{ActorRef, Props}
import akka.http.scaladsl.Http
import akka.stream.scaladsl.Flow
import net.liftweb.json._
import scala.concurrent.ExecutionContext.Implicits.global

object Esp01Dht11HttpTemperatureSensor {

  def props(sensorName: String, sensorIp: String, sensorPort: String, readDuration: Int, readDelay: Int): Props = Props(new Esp01Dht11HttpTemperatureSensor(sensorName: String, sensorIp: String, sensorPort: String, readDuration: Int, readDelay: Int))

}

case class Dht11TemperatureResponse(temperature: String)

class Esp01Dht11HttpTemperatureSensor(sensorName: String, sensorIp: String, sensorPort: String, readDuration: Int, readDelay: Int) extends Esp01Dht11HttpSensorBase(sensorName: String, readDuration: Int, readDelay: Int) {

  override val readingType = "temperature"
  lazy val pool = Http().newHostConnectionPool[Int](sensorIp, sensorPort.toInt)

  override def parseDefaultReading = {
    parse("{\"temperature\" : \"0\"}")
  }

  def preProcessReading = {
    Flow[JValue]
      .map { e: JValue =>
        implicit val formats = net.liftweb.json.DefaultFormats
        e.extract[Dht11TemperatureResponse]
      }
  }

  def extractReading =
    Flow[Dht11TemperatureResponse].map { e: Dht11TemperatureResponse => e.temperature.toInt }

  override def pushDataUpstream(sender: ActorRef): Unit = {

    sensorDataSourceLocal.get("/temperature", pool)
      .via(preProcessReading)
      .via(extractReading)
      .via(processStream)
      .to(sensorPushSink(sender))
      .run()
  }

}