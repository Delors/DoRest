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

import java.io._
import java.nio.charset._

trait RESTInterface extends Handler {

    private val getHandlers = scala.collection.mutable.ListBuffer[ResponseBodyInitializer[_]]()
    private val postHandlers = scala.collection.mutable.ListBuffer[PostHandler]()

    private val putHandlers = scala.collection.mutable.ListBuffer[(MediaType.Value, () => Unit)]()
    private val deleteHandlers = scala.collection.mutable.ListBuffer[(MediaType.Value, () => Unit)]() // a delete response can have a body

    protected val responseHeaders = new DefaultResponseHeaders() // TODO make uninitialized...

    protected var responseCode = 200 // OK // TODO make uninitialized... (it is set by the subsequent code in most cases anyway..)

    protected var responseBody: ResponseBody = EmptyResponseBody // TODO make uninitialized...

    def processRequest(requestBody: InputStream): Response = {
        method match {
            case GET if !getHandlers.isEmpty => {
                val mediaType = requestHeaders.getFirst("accept")
                if ((mediaType eq null) || (mediaType == "*/*")) {
                    getHandlers.head.initResponseBody()
                    return new DefaultResponse(responseCode, responseHeaders, responseBody)
                }
                // TODO improve the search for a matching handler
                getHandlers.find(_.mediaType.toString == mediaType) match {
                    case Some(rbi) => {
                        rbi.initResponseBody()
                        setContentTypeResponseHeader(responseBody)
                        return new DefaultResponse(responseCode, responseHeaders, responseBody)
                    }
                    case _ =>
                        return UnsupportedMediaTypeResponse
                }
            }
            case POST if !postHandlers.isEmpty => {
                // TODO nearly everything... matching...
                val postHandler = postHandlers.head
                postHandler.requestBodyHandler._2(None, requestBody)
                postHandler.responseBodyInitializer.initResponseBody()
                return new DefaultResponse(responseCode, responseHeaders, responseBody)
            }
            case _ => {
                var supportedMethods: List[HTTPMethod] = Nil
                if (!getHandlers.isEmpty)
                    supportedMethods = GET :: supportedMethods;

                if (!putHandlers.isEmpty)
                    supportedMethods = PUT :: supportedMethods;

                if (!postHandlers.isEmpty)
                    supportedMethods = POST :: supportedMethods;

                if (!deleteHandlers.isEmpty)
                    supportedMethods = DELETE :: supportedMethods;

                new SupportedMethodsResponse(supportedMethods)
            }
        }
    }

    private def setContentTypeResponseHeader(responseBody: ResponseBody) {
        responseBody.contentType match {
            case Some((mediaType, None)) => {
                val contentType = mediaType.toString
                responseHeaders.set("Content-Type", contentType)
            }
            case Some((mediaType, Some(charset))) => {
                val contentType = mediaType.toString + "; charset=" + charset.displayName
                responseHeaders.set("Content-Type", contentType)
            }
            case _ => /*OK*/
        }
    }

    /**
     * This object enables us to write code in a more declarative meaning:<br>
     * <code>
     * get accepts …
     * </code>
     * accepts is a method that has the same signature as the {@link #get} method and
     * just forwards the call.
     */
    final object get {
        def requests(t: RepresentationFactory[MediaType.Value]) {
            getHandlers += ResponseBodyInitializer(t.mediaType) {
                RESTInterface.this.responseBody = t.createRepresentation()
            }
        }
    }

    class PostHandler(val requestBodyHandler: (MediaType.Value, (Option[Charset], InputStream) => Unit)) {

        var responseBodyInitializer: ResponseBodyInitializer[MediaType.Value] = _

        def returns[M <: MediaType.Value](t: RepresentationFactory[M]) {
            responseBodyInitializer = ResponseBodyInitializer(t.mediaType) {
                RESTInterface.this.responseBody = t.createRepresentation()
            }
            postHandlers += this
        }
    }

    final object post {
        def receives(requestBodyHandler: (MediaType.Value, (Option[Charset], InputStream) => Unit)) = {
            new PostHandler(requestBodyHandler)
        }

    }

}

class ResponseBodyInitializer[+M <: MediaType.Value](val mediaType: M,
                                                     val initResponseBody: () => Unit)

object ResponseBodyInitializer {

    def apply[M <: MediaType.Value](mediaType: M)(initResponseBody: => Unit) =
        new ResponseBodyInitializer[M](mediaType, () => initResponseBody)
}

trait Representation[+M <: MediaType.Value] extends ResponseBody

class RepresentationFactory[M <: MediaType.Value](val mediaType: M,
                                                  val createRepresentation: () => Representation[M])

object RepresentationFactory {

    def apply[M <: MediaType.Value](mediaType: M)(createRepresentation: => Representation[M]) =
        new RepresentationFactory(mediaType, () => createRepresentation)
}

class UTF8BasedRepresentation[M <: MediaType.Value](val mediaType: M, val utf8String: Array[Byte]) extends Representation[M] {

    import Utils._

    def contentType = Some((mediaType, Some(UTF8)))

    def length = utf8String.length

    def write(responseBodyOutputStream: OutputStream) {
        responseBodyOutputStream.write(utf8String)
    }
}







