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

/** Common superclass of all HTTP Method representations.
  *
  * The list of all HTTP Methods is defined in
  * [[http://www.w3.org/Protocols/rfc2616/rfc2616.html RFC 2616: "Hypertext Transfer Protocol -- HTTP/1.1"]]
  * and in
  * [[http://tools.ietf.org/html/rfc5789 RFC 5789: "PATCH Method for HTTP"]].
  *
  * @author Michael Eichberg
  * @author Mateusz Parzonka
  */
sealed trait HTTPMethod {

    /** This HTTP method's name.
      *
      * @see [[http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html HTTP/1.1 Method Definitions]]
      */
    def name: String
}

/** Registry, factory and extractor for HTTPMethod objects.
  *
  * By default all standard HTTP methods (GET, PUT, POST, DELETE, PATCH, OPTIONS, HEAD, CONNECT and TRACE) are
  * already registered.
  *
  * To add support for extension methods (cf.
  * [[http://www.w3.org/Protocols/rfc2616/rfc2616-sec5.html#sec5.1.1 HTTP/1.1 Method]]) create a new
  * case object that inherits from [[org.dorest.server.HTTPMethod]] and that represents your extension
  * method. Afterwards register the new `HTTPMethod` object using the `register` method.
  *
  * @author Michael Eichberg
  */
object HTTPMethod {

    private[this] val methods = scala.collection.mutable.Map[String, HTTPMethod]()

    /** Register a new, additional HTTPMethod.
      *
      * @note The standard HTTP methods are already registered.
      */
    def register(httpMethod: HTTPMethod) {
        require(!methods.contains(httpMethod.name))

        methods += ((httpMethod.name, httpMethod))
    }

    // register all standard HTTP methods
    register(GET)
    register(POST)
    register(PUT)
    register(DELETE)
    register(PATCH)
    register(OPTIONS)
    register(HEAD)
    register(CONNECT)
    register(TRACE)

    /** Returns the HTTPMethod object that represents the given method. If the specified method name is
      * not known `None` is returned. This might happen if the server is attacked or if an application specific
      * HTTP method was not registered.
      */
    def apply(methodName: String): Option[HTTPMethod] = methods.get(methodName)

}

/** Represents the GET HTTP method.
  *
  * @see [[http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.3 HTTP/1.1 Method Definitions - GET]]
  * @author Michael Eichberg
  */
case object GET extends HTTPMethod { def name = "GET" }

/** Represents the POST HTTP method.
  *
  * @see [[http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.5 HTTP/1.1 Method Definitions - POST]]
  * @author Michael Eichberg
  */
case object POST extends HTTPMethod { def name = "POST" }

/** Represents the PUT HTTP method.
  *
  * @see [[http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.9 HTTP/1.1 Method Definitions - PUT]]
  * @author Michael Eichberg
  */
case object PUT extends HTTPMethod { def name = "PUT" }

/** Represents the DELETE HTTP method.
  *
  * @see [[http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.7 HTTP/1.1 Method Definitions - DELETE]]
  * @author Michael Eichberg
  */
case object DELETE extends HTTPMethod { def name = "DELETE" }

/** Represents the PATCH HTTP method.
  *
  * @see [[http://tools.ietf.org/html/rfc5789 RFC 5789: "PATCH Method for HTTP"]]
  * @author Michael Eichberg
  */
case object PATCH extends HTTPMethod { def name = "PATCH" }

/** Represents the OPTIONS HTTP method.
  *
  * @see [[http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.2 HTTP/1.1 Method Definitions - OPTIONS]]
  * @author Michael Eichberg
  */
case object OPTIONS extends HTTPMethod { def name = "OPTIONS" }

/** Represents the HEAD HTTP method.
  *
  * @see [[http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.4 HTTP/1.1 Method Definitions - HEAD]]
  * @author Michael Eichberg
  */
case object HEAD extends HTTPMethod { def name = "HEAD" }

/** Represents the CONNECT HTTP method.
  *
  * @see [[http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.9 HTTP/1.1 Method Definitions - CONNECT]]
  * @author Michael Eichberg
  */
case object CONNECT extends HTTPMethod { def name = "CONNECT" }

/** Represents the TRACE HTTP method.
  *
  * @see [[http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.8 HTTP/1.1 Method Definitions - TRACE]]
  * @author Michael Eichberg
  */
case object TRACE extends HTTPMethod { def name = "TRACE" }
