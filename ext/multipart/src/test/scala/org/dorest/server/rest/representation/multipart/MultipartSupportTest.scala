package org.dorest.server
package rest
package representation.multipart

import java.io._
import org.apache.commons.io.IOUtils
import org.dorest.client.DigestAuth
import org.dorest.client.SimpleClient
import org.dorest.server.jdk.Server
import org.dorest.server.rest._
import org.dorest.server._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.BeforeAndAfterAll
import org.scalatest.FlatSpec
import org.dorest.client.Entity
import org.dorest.client.Part

/**
 * @author Mateusz Parzonka
 */
object MultipartSupportTestServer extends Server(9998) {

import org.apache.commons.io.{ IOUtils, FileUtils }
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

    post of Multipart returns XML {
      println("uploading")
      println(parts(1).getFieldName)
//      writeByteStream(inputStream, "temp/uploaded (bytestream).pdf")
      <success/>
    }
    
  }

}

/**
 * @author Mateusz Parzonka
 */
@RunWith(classOf[JUnitRunner])
class MultipartSupportTest extends FlatSpec with ShouldMatchers with BeforeAndAfterAll {

  override def beforeAll(configMap: Map[String, Any]) {
    println("Starting MultipartSupportTest")
    MultipartSupportTestServer
  }

  val post = SimpleClient.post(Map("Accept" -> "application/xml"), new DigestAuth("somebody", "password")) _

  "MultipartSupport" should "allow a client to POST a pdf" in {
    val response = post("http://localhost:9998/upload",  
        Entity("someString" -> Part("foo", "text/plain"), 
       "someFile" -> Part(new File("src/test/resources/test.pdf"), "application/pdf")))
//    response.statusCode should equal { 200 }
//    response.contentType should equal { "application/xml; charset=UTF-8" }
  }
  
}

