/**
  * Reads HTTP request from ESP-01 run web server, that collects DHT-11 sensor data.
  **/
package lv.arvissk.governor.base.modules.sensors.drivers

import lv.arvissk.governor.base.modules.sensors.SensorsProtocol

import scala.concurrent._
import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Success, Try}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes, HttpMethods}
import akka.http.scaladsl.client.RequestBuilding._
import akka.actor.{Props, Cancellable}
import akka.stream.{Materializer, ActorMaterializer}
import akka.stream.scaladsl.{Flow, Source}
import net.liftweb.json._

object Esp01Dht11HttpSensorBase {

  def props(sensorName: String, readDuration: Int, readDelay: Int): Props = Props(new Esp01Dht11HttpSensorBase(sensorName: String, readDuration: Int, readDelay: Int))

}

class Esp01Dht11HttpSensorBase(sensorName: String, readDuration: Int, readDelay: Int) extends GenericSensor(sensorName: String) {

  import SensorsProtocol._

  implicit val system = context.system
  implicit val materializer = ActorMaterializer()

  override def receive = {
    case InitSensor =>
      sender ! SensorInitSuccessful(sensorName)
      pushDataUpstream(sender)
  }

  def parseDefaultReading = {
    parse("{\"reading\" : \"0\"}")
  }

  object sensorDataSourceLocal {

    def get(modelID: String, pool: Flow[(HttpRequest, Int), (Try[HttpResponse], Int), Http.HostConnectionPool])
           (implicit ec: ExecutionContext, mat: Materializer): Source[JValue, Cancellable] = {
      val uri = modelID
      val req = HttpRequest(uri = modelID)
      Source.tick(FiniteDuration(readDelay, TimeUnit.SECONDS), FiniteDuration(readDuration, TimeUnit.SECONDS), (req, 0)).via(pool)
        .map {
          case (Success(resp), _) =>

            val jsonStringFuture = resp.entity.toStrict(5 seconds).map(_.data.decodeString("UTF-8"))
            val jsonString: String = Await.result(jsonStringFuture, 3 seconds)
            parse(jsonString)

          case _ =>
            parseDefaultReading
        }
    }
  }

}