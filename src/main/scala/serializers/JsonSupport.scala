package github.interaction.docsearcher.serializers

import java.text.SimpleDateFormat

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import github.interaction.docsearcher.entities.{DocumentResult, PaginationResult}
import org.json4s.ext.JodaTimeSerializers
import org.json4s.{DefaultFormats, Formats, native}
import spray.json.{DefaultJsonProtocol, JsonFormat}

trait JsonSupport extends Json4sSupport {

  implicit val serialization = native.Serialization

  implicit def json4sFormats: Formats = customDateFormat ++ JodaTimeSerializers.all ++ CustomSerializers.all

  val customDateFormat = new DefaultFormats {
    override def dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
  }
  
}

// 这个类不建议使用, 放这儿只是个演示作用, 虽然是照官方文档配置，但是使用进去的时候有些诡异的问题
// https://github.com/spray/spray-json
// http://doc.akka.io/docs/akka/2.4.11/scala/http/common/json-support.html#akka-http-spray-json
trait MyJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  // jsonFormat<n>: n stands number of params in the type class
  implicit val documentResultFormat = jsonFormat2(DocumentResult)

  implicit def paginationResultFormat[A: JsonFormat] = jsonFormat2(PaginationResult.apply[A])

}
