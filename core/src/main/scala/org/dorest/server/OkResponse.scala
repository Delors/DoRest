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
 * Encapsulates an OK response.
 *  
 * From RFC 2616 (HTTP/1.1):
 *  
 * The request has succeeded. The information returned with the response is dependent on the method used in the request, for example:
 *
 * GET an entity corresponding to the requested resource is sent in the response;
 * 
 * HEAD the entity-header fields corresponding to the requested resource are sent in the response without any message-body;
 * 
 * POST an entity describing or containing the result of the action;
 * 
 * TRACE an entity containing the request message as received by the end server.
 * 
 * For further details see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616.html">Hypertext Transfer Protocol -- HTTP/1.1 (RFC 2616)</a>
 * 
 * @author Michael Eichberg
 */
abstract class OkResponse extends Response {

    final def code = 200 //OK
    
}


object OkResponse {

    def apply(responseHeaders: ResponseHeaders, responseBody: Option[ResponseBody]) =
        new OkResponse {

            def headers: ResponseHeaders = responseHeaders

            def body: Option[ResponseBody] = responseBody

        }

}

































