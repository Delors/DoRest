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

import java.nio.charset.Charset
import java.io._

/**
 * A response object encapsulates a specific representation that will be send back to the client.
 */
trait Response extends {

    /**
     * The status code of the response.
     *
     * Go to: <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10">HTTP Status Codes</a> for further
     * details.
     */
    def code: Int

    /**
     * A response's headers. The response headers for the Content-type and Content-length are automatically set based
     * on the response body.
     *
     * '''Remark''': ResponseHeaders must not be null and mutable.
     */
    def headers: ResponseHeaders

    /**
     * The body that is send back to the client.
     */
    def body: Option[ResponseBody]
}


object Response {

    def apply(responseCode: Int, responseHeaders: ResponseHeaders, responseBody: Option[ResponseBody]) =
        new Response {
            val code: Int = responseCode
            val headers: ResponseHeaders = responseHeaders
            val body: Option[ResponseBody] = responseBody
        }

}


/**
 * A response's headers.
 */
trait ResponseHeaders {

    /**
     * Sets the value of the specified response header.
     *
     * Cf. <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14">HTTP Header Fields</a>.
     */
    def set(key: String, value: String): Unit

    /**
     * Enables you to iterate over all response headers.
     */
    def foreach(f: ((String, String)) => Unit): Unit
}


/**
 * Map based implementation of the ResponseHeaders trait.
 */
class DefaultResponseHeaders(private var headers: Map[String, String] = Map())
        extends ResponseHeaders {

    def this(header: Tuple2[String, String]) {
        this (Map() + header)
    }

    def this(headers: List[Tuple2[String, String]]) {
        this (Map() ++ headers)
    }

    def set(key: String, value: String): Unit = {
        headers = headers.updated(key, value)
    }

    def foreach(f: ((String, String)) => Unit) {
        headers.foreach(f)
    }

}

/**
 * Encapsulates a response's body.
 */
trait ResponseBody {

    /**
     * The body's content type (and charset).
     */
    def contentType: Option[(MediaType.Value, Option[Charset])]

    /**
     * The number of bytes that will be send back.
     */
    def length: Int

    /**
     * Called by the framework – after sending the HTTP header – to write
     * out the specific representation as the response's body.
     */
    def write(responseBody: OutputStream): Unit
}


trait OkResponse extends Response {

    final def code = 200 //OK
}

object OkResponse {

    def apply(responseHeaders: ResponseHeaders, responseBody: Option[ResponseBody]) =
        new OkResponse {

            def headers: ResponseHeaders = responseHeaders

            def body: Option[ResponseBody] = responseBody

        }

}


class SupportedMethodsResponse(val allowedMethods: List[HTTPMethod]) extends Response {

    def this(allowedMethod: HTTPMethod) {
        this (allowedMethod :: Nil)
    }

    final def code = 405

    val headers = new DefaultResponseHeaders(("Allow", allowedMethods.mkString(", ")))

    def body = None

}

object SupportedMethodsResponse {

    def apply(allowedMethods: List[HTTPMethod]) = new SupportedMethodsResponse(allowedMethods)

}


/**
 * Use an error response only to signal severe errors. The text will be send using the charset US-ASCII.
 *
 * @author Michael Eichberg
 */
class ErrorResponse(val code: Int, val text: String) extends Response {

    val headers = new DefaultResponseHeaders()

    val body = Some(new ResponseBody {

        private val response: Array[Byte] = Charset.forName("US-ASCII").encode(text).array()

        def contentType = Some((MediaType.TEXT, Some(Charset.forName("US-ASCII"))))

        def length: Int = response.length

        def write(responseBody: OutputStream) {
            responseBody.write(response)
        }
    })
}

object ErrorResponse {
    def apply(code: Int, text: String) = new ErrorResponse(code, text)
}

class Forbidden(text: String) extends ErrorResponse(403, text)

object Forbidden {

    def apply(text: String) = new Forbidden(text)
}


abstract class PlainResponse(val code: Int) extends Response {

    def headers = new DefaultResponseHeaders()

    def body = None
}

object BadRequest extends PlainResponse(400)

object NotFoundResponse extends PlainResponse(404)

object UnsupportedMediaTypeResponse extends PlainResponse(415)