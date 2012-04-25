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
 * '''<a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.6">HTTP 1.1 Specification Method Not Allowed (405)</a>''': The
 * method specified in the Request-Line is not allowed for the resource identified by the Request-URI. The response
 * MUST include an Allow header containing a list of valid methods for the requested resource.
 *
 * @author Michael Eichberg
 */
class SupportedMethodsResponse(val allowedMethods: List[HTTPMethod], val code: Int = 200) extends Response {

    def this(allowedMethod: HTTPMethod) {
        this (allowedMethod :: Nil)
    }
    
    val headers = new DefaultResponseHeaders(("Allow", allowedMethods.mkString(", ")))

    def body = None

}



object SupportedMethodsResponse {

    def apply(allowedMethods: List[HTTPMethod]) = new SupportedMethodsResponse(allowedMethods)

}



































