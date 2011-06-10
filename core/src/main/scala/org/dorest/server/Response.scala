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

trait Response extends {

    def code: Int
    def headers: ResponseHeaders
    def body: ResponseBody
}

trait ResponseHeaders {
    def set(key: String, value: String): Unit
    def foreach(f: ((String, String)) => Unit): Unit
}

object EmptyResponseHeaders extends ResponseHeaders {
    def set(key: String, value: String): Unit = throw new Error()
    def foreach(f: ((String, String)) => Unit): Unit = {}
}

/**
 * Map based implementation of the ResponseHeaders trait.
 */
class DefaultResponseHeaders(private var headers: Map[String, String] = Map())
        extends ResponseHeaders {

    def this(header: Tuple2[String, String]) {
        this(Map() + header)
    }

    def this(headers: List[Tuple2[String, String]]) {
        this(Map() ++ headers)
    }

    def set(key: String, value: String): Unit = {
        headers = headers.updated(key, value)
    }

    def foreach(f: ((String, String)) => Unit) {
        headers.foreach(f)
    }

}

trait ResponseBody {

    def contentType: Option[(MediaType.Value, Option[Charset])]

    def length: Int

    /**
     * Called by the framework – after sending the HTTP header – to write
     * out the specific representation as the response's body.
     */
    def write(responseBody: OutputStream): Unit
}

object EmptyResponseBody extends ResponseBody {
    def contentType = None
    def length = 0
    def write(responseBody: OutputStream): Unit = { /*NOTHING TO DO*/ }
}

class DefaultResponse(
    val code: Int,
    val headers: ResponseHeaders,
    val body: ResponseBody) extends Response

class SupportedMethodsResponse(allowedMethods: List[HTTPMethod.Value])
        extends DefaultResponse(
            405,
            new DefaultResponseHeaders(("Allow", allowedMethods.mkString(", "))),
            EmptyResponseBody
        ) {

    def this(allowedMethod: HTTPMethod.Value) {
        this(allowedMethod :: Nil)
    }
}

abstract class OkResponse extends Response {

    def code = 200 //OK
}

class TextResponse(val code: Int, val text: String) extends Response {

    import Utils._

    val headers = new DefaultResponseHeaders()

    lazy val body = new ResponseBody {
        private val response: Array[Byte] = toUTF8(text)

        def contentType = Some((MediaType.TEXT, Some(UTF8)))

        def length: Int = response.length

        def write(responseBody: OutputStream) {
            responseBody.write(response)
        }
    }
}

abstract class EmptyResponse(code: Int)
    extends DefaultResponse(
        code,
        EmptyResponseHeaders,
        EmptyResponseBody)

object BadRequest extends EmptyResponse(400)

class Forbidden(text: String) extends TextResponse(403, text)

object NotFoundResponse
    extends EmptyResponse(404)

object UnsupportedMediaTypeResponse
    extends EmptyResponse(415)