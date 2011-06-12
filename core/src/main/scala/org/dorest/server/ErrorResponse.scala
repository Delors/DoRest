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
 * Use an error response only to signal severe errors. The text will be send using US-ASCII.
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














