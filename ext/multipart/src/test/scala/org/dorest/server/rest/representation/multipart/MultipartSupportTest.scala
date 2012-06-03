/*
   Copyright 2011 Michael Eichberg et al

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.dorest.server
package rest
package representation.multipart

import java.io._

import org.apache.commons.io.IOUtils
import org.dorest.client.DigestAuth
import org.dorest.client.Entity
import org.dorest.client.Part
import org.dorest.client.SimpleClient
import org.dorest.server.jdk.JDKServer
import org.dorest.server.rest._
import org.dorest.server._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.BeforeAndAfterAll
import org.scalatest.FlatSpec

/**
 * @author Mateusz Parzonka
 */
object MultipartSupportTestServer extends JDKServer(9998) {

    import org.apache.commons.io.{ IOUtils, FileUtils }

    this addPathMatcher ((path) ⇒ if ("/upload" == path) Some((query) => Some(new UploadResource)) else None)

    start()

    class UploadResource extends RESTInterface with XMLSupport with MultipartSupport {

        def writeByteStream(inputStream: InputStream, file: String) = {
            val outputStream = new FileOutputStream(file)
            IOUtils.copy(inputStream, outputStream)
            outputStream.close()
        }

        post of Multipart returns XML {
            for (part ← multipartIterator) {
                part match {
                    case part @ FormField("someString") ⇒ println(part.content)
                    case part @ Data("someFile", MediaType.APPLICATION_PDF) ⇒ {
                        val fos = new FileOutputStream(new File("target/test-uploaded.pdf"))
                        var read: Int = 0
                        val stream = part.openStream
                        while ({ read = stream.read; read != -1 }) {
                            fos.write(read)
                        }
                        stream.close()
                        fos.flush()
                        fos.close()
                    }
                }
            }
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
            Entity("someString" -> Part("foo"),
                "someFile" -> Part(new File("src/test/resources/test.pdf"), "application/pdf")))
    }

}

