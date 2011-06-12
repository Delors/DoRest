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