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

/** The list of all HTTP Methods as defined by "Hypertext Transfer Protocol -- HTTP/1.1 (RFC 2616)" and also
  * "PATCH Method for HTTP (RFC 5789)".
  *
  * @author Michael Eichberg
  * @author Mateusz Parzonka
  */
sealed trait HTTPMethod

object HTTPMethod {

    def apply(methodName: String) = methodName match {
        case "GET"     ⇒ GET
        case "POST"    ⇒ POST
        case "PUT"     ⇒ PUT
        case "DELETE"  ⇒ DELETE
        case "PATCH"   ⇒ PATCH
        case "OPTIONS" ⇒ OPTIONS
        case "HEAD"    ⇒ HEAD
        case "CONNECT" ⇒ CONNECT
    }

    def unapply(method: HTTPMethod): String = method match {
        case GET     ⇒ "GET"
        case POST    ⇒ "POST"
        case PUT     ⇒ "PUT"
        case DELETE  ⇒ "DELETE"
        case PATCH   ⇒ "PATCH"
        case OPTIONS ⇒ "OPTIONS"
        case HEAD    ⇒ "HEAD"
        case CONNECT ⇒ "CONNECT"
    }

}

case object GET extends HTTPMethod

case object POST extends HTTPMethod

case object PUT extends HTTPMethod

case object DELETE extends HTTPMethod

case object PATCH extends HTTPMethod

case object OPTIONS extends HTTPMethod

case object HEAD extends HTTPMethod

case object CONNECT extends HTTPMethod
