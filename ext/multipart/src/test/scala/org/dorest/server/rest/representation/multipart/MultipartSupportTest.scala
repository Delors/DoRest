package org.dorest.server
package rest
package representation.multipart

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.ShouldMatchers
import org.dorest.client.SimpleClient
import org.dorest.client.Entity
import org.dorest.server.jdk.Server
import org.dorest.server._
import rest._
import java.io._
import org.apache.commons.io.{ IOUtils, FileUtils }
import org.dorest.client.Part

/**
 * Starts a test server which reads PDFs, PNGs and textfiles located in test/resources and stores them to /temp.
 *
 * @author Mateusz Parzonka
 */
object MultipartSupportTestServer extends Server(9999) {

  this register new HandlerFactory[UploadResource] {
    path { "/upload" }
    def create = new UploadResource
  }

  start()

  class UploadResource extends RESTInterface with XMLSupport with MultipartSupport {

    def writeByteStream(inputStream: InputStream, file: String) = {
      val outputStream= new FileOutputStream(file)
      IOUtils.copy(inputStream, outputStream)
      outputStream.close()
    }

    put of Multipart returns XML {
      println("uploading")
      println(parts(1).getFieldName)
//      writeByteStream(inputStream, "temp/uploaded (bytestream).pdf")
      <success/>
    }

  }

}

/**
 * PDFs, PNGs and textfiles located in test/resources are up- and downloaded via SimpleClient and stored in /temp.
 * The "/temp"-directory is not deleted after the test, to enable control additional of results and proper charset-conversions.
 * 
 * @author Mateusz Parzonka
 */
@RunWith(classOf[JUnitRunner])
class MultipartSupportTest extends FlatSpec with ShouldMatchers with BeforeAndAfterAll {

  override def beforeAll(configMap: Map[String, Any]) {
    println("Starting MultipartSupportTest")
    MultipartSupportTestServer
  }

  val put = SimpleClient.put(Map("Accept" -> "application/xml")) _

  "MultipartSupport" should "allow a client to PUT a pdf" in {
    val response = put("http://localhost:9999/upload", Entity("someFile" -> Part(new File("src/test/resources/test.pdf"), "application/pdf")))
//    response.statusCode should equal { 200 }
//    response.contentType should equal { "application/xml; charset=UTF-8" }
  }
  
}

