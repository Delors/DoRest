package org.dorest.server
package rest
package representation.stream

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.ShouldMatchers

import org.dorest.client.SimpleClient
import org.dorest.client.Entity
import org.dorest.server.jdk.JDKServer
import org.dorest.server._
import rest._
import java.io._

import org.apache.commons.io.{ IOUtils, FileUtils }

/**
 * Starts a test server which reads PDFs, PNGs and textfiles located in test/resources and stores them to /temp.
 *
 * @author Mateusz Parzonka
 */
object StreamSupportTestServer extends JDKServer(9999) {

    addURIMatcher(
        / {
            case "bytestream" ⇒ / {
                case MATCHED() ⇒ new ByteStreamResource
                case "bytes"   ⇒ new ByteStreamBytesResource
            }
            case "characterstream" ⇒ / {
                case STRING(encoding) ⇒ new CharacterStreamResource(encoding)
            }
        }
    )

    start()

    class ByteStreamResource extends RESTInterface with XMLSupport with StreamSupport {

        def byteStream(filePath: String): Option[(InputStream, Int)] = {
            val file = new File(filePath);
            Some((new BufferedInputStream(new FileInputStream(file)), file.length.toInt))
        }

        get returns ByteStream(MediaType.APPLICATION_PDF) {
            byteStream("src/test/resources/test.pdf")
        }

        get returns ByteStream(MediaType.IMAGE_PNG) {
            byteStream("src/test/resources/test.png")
        }

        def writeByteStream(inputStream: InputStream, file: String) = {
            val outputStream = new FileOutputStream(file)
            IOUtils.copy(inputStream, outputStream)
            outputStream.close()
        }

        put of InputStream(MediaType.APPLICATION_PDF) returns XML {
            writeByteStream(inputStream, "temp/uploaded (bytestream).pdf")
            <success/>
        }

        put of InputStream(MediaType.IMAGE_PNG) returns XML {
            writeByteStream(inputStream, "temp/uploaded (bytestream).png")
            <success/>
        }

    }

    class ByteStreamBytesResource extends RESTInterface with XMLSupport with StreamSupport {

        put of InputStream(MediaType.APPLICATION_PDF) returns XML {
            FileUtils.writeByteArrayToFile(new java.io.File("temp/uploaded (bytes).pdf"), bytes)
            <success/>
        }

        put of InputStream(MediaType.IMAGE_PNG) returns XML {
            FileUtils.writeByteArrayToFile(new java.io.File("temp/uploaded (bytes).png"), bytes)
            <success/>
        }
    }

    /**
     * We write files with different encodings as UTF-8
     */
    class CharacterStreamResource(var encoding: String) extends RESTInterface with XMLSupport with StreamSupport {

        put of InputStream(MediaType.APPLICATION_XML) returns XML {
            FileUtils.writeStringToFile(new File("temp/uploaded_%s->utf-8 (characterStream).xml" format encoding),
                IOUtils.toString(reader), "UTF-8")
            <success/>
        }

        put of InputStream(MediaType.TEXT_PLAIN) returns XML {
            FileUtils.writeStringToFile(new File("temp/uploaded_%s->utf-8 (characterStream).txt" format encoding),
                IOUtils.toString(reader), "UTF-8")
            <success/>
        }

        get returns CharacterStream(MediaType.APPLICATION_XML) {
            val fis = new FileInputStream("src/test/resources/test_utf-8.xml");
            val isr = new InputStreamReader(fis, "UTF-8");
            val bsr = new BufferedReader(isr);
            (bsr, 60)
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
class StreamSupportTest extends FlatSpec with ShouldMatchers with BeforeAndAfterAll {

    override def beforeAll(configMap: Map[String, Any]) {
        println("Starting StreamSupportTest")
        StreamSupportTestServer
    }

    def get(contentType: String) = SimpleClient.get(Map("Accept" -> contentType)) _
    val put = SimpleClient.put(Map("Accept" -> "application/xml")) _

    "StreamSupport with bytestream" should "allow a client to GET a pdf" in {
        val response = get("application/pdf")("http://localhost:9999/bytestream")
        response.statusCode should equal { 200 }
        response.contentType should equal { "application/pdf; charset=UTF-8" }
        org.apache.commons.io.FileUtils.writeByteArrayToFile(new java.io.File("temp/downloaded.pdf"), response.bytes)
    }

    it should "allow a client to GET a png" in {
        val response = get("image/png")("http://localhost:9999/bytestream")
        response.statusCode should equal { 200 }
        response.contentType should equal { "image/png; charset=UTF-8" }
        FileUtils.writeByteArrayToFile(new java.io.File("temp/downloaded.png"), response.bytes)
    }

    it should "allow a client to PUT a pdf" in {
        val response = put("http://localhost:9999/bytestream", Entity(new File("src/test/resources/test.pdf"), "application/pdf"))
        response.statusCode should equal { 200 }
        response.contentType should equal { "application/xml; charset=UTF-8" }
    }

    it should "allow a client to PUT a 2 MB pdf" in {
        val response = put("http://localhost:9999/bytestream", Entity(new File("src/test/resources/Better Builds With Maven.pdf"), "application/pdf"))
        response.statusCode should equal { 200 }
        response.contentType should equal { "application/xml; charset=UTF-8" }
    }

    it should "allow a client to PUT a png" in {
        val response = put("http://localhost:9999/bytestream", Entity(new File("src/test/resources/test.png"), "image/png"))
        response.statusCode should equal { 200 }
        response.contentType should equal { "application/xml; charset=UTF-8" }
    }

    "StreamSupport with bytes" should "allow a client to PUT a pdf" in {
        val response = put("http://localhost:9999/bytestream/bytes", Entity(new File("src/test/resources/test.pdf"), "application/pdf"))
        response.statusCode should equal { 200 }
        response.contentType should equal { "application/xml; charset=UTF-8" }
    }

    it should "allow a client to PUT a png" in {
        val response = put("http://localhost:9999/bytestream/bytes", Entity(new File("src/test/resources/test.png"), "image/png"))
        response.statusCode should equal { 200 }
        response.contentType should equal { "application/xml; charset=UTF-8" }
    }

    /*"StreamSupport with characterStream"*/ ignore should "allow a client to PUT xml using UTF-8" in {
        val response = put("http://localhost:9999/characterstream/UTF-8", Entity(new File("src/test/resources/test_utf-8.xml"), "application/xml; charset=UTF-8"))
        response.statusCode should equal { 200 }
        response.contentType should equal { "application/xml; charset=UTF-8" }
    }

    ignore should "allow a client to PUT xml using ISO-8859-1" in {
        val response = put("http://localhost:9999/characterstream/ISO-8859-1", Entity(new File("src/test/resources/test_ISO-8859-1.xml"), "application/xml; charset=ISO-8859-1"))
        response.statusCode should equal { 200 }
        response.contentType should equal { "application/xml; charset=UTF-8" }
    }

    ignore should "allow a client to PUT text using UTF-8" in {
        val response = put("http://localhost:9999/characterstream/UTF-8", Entity(new File("src/test/resources/test_utf-8.txt"), "text/plain; charset=UTF-8"))
        response.statusCode should equal { 200 }
        response.contentType should equal { "application/xml; charset=UTF-8" }
    }

    ignore should "allow a client to PUT text using ISO-8859-1" in {
        val response = put("http://localhost:9999/characterstream/ISO-8859-1", Entity(new File("src/test/resources/test_ISO-8859-1.txt"), "text/plain; charset=ISO-8859-1"))
        response.statusCode should equal { 200 }
        response.contentType should equal { "application/xml; charset=UTF-8" }
    }

    ignore should "allow a client to GET xml using UTF-8" in {
        val response = get("application/xml")("http://localhost:9999/characterstream/UTF-8")
        response.statusCode should equal { 200 }
        response.contentType should equal { "application/xml; charset=UTF-8" }
        println(response.body)
    }

}

