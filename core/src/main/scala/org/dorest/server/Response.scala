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

/**
 * A response object encapsulates a request's response.
 *
 * The precise/expected structure of a response depends on the request and is defined by
 * [[http://www.w3.org/Protocols/rfc2616/rfc2616.html RFC 2616]].
 *
 * @author Michael Eichberg
 */
trait Response {

    /**
     * The status code of the response.
     *
     * @see [[http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10 HTTP Status Codes]]
     */
    def code: Int

    /**
     * A response's headers.
     *
     * @note The HTTP headers Content-Type and Content-Length are automatically set based on the
     * response body.
     * @return This response's header fields (non-null).
     */
    def headers: ResponseHeaders

    /**
     * The body that is send back to the client.
     */
    def body: Option[ResponseBody]
}
/**
 * Factory object for creating a [[org.dorest.server.Response]] object.
 */
object Response {

    /**
     * Creates a new, generic [[org.dorest.server.Response]] object.
     *
     * @param responseCode A valid HTTP response code. See also [[org.dorest.server.Response]].code
     * @param responseHeaders The HTTP response headers. See also [[org.dorest.server.Response]].headers
     * @param responseBody The body of the HTTP response. See also [[org.dorest.server.Response]].body
     * @return A new response object.
     */
    def apply(responseCode: Int, responseHeaders: ResponseHeaders, responseBody: Option[ResponseBody]) = {
        require(responseHeaders ne null)
        require(responseBody match {
            case Some(_) ⇒ true // the body may be lazily initialized; hence we do not check that `body.length > 0`
            case None    ⇒ true
            case null    ⇒ false
        });

        new Response {
            val code: Int = responseCode
            val headers: ResponseHeaders = responseHeaders
            val body: Option[ResponseBody] = responseBody
        }
    }

}