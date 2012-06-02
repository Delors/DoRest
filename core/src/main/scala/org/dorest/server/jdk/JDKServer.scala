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
package jdk

import log._
import rest._

import com.sun.net.httpserver._
import java.net._
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/** Simple stand alone server that uses the (SUN) JDKs built-in HTTP server.
  *
  * This implementation of a server is primarily provided for testing and
  * debugging applications. It is not intended to be used for real world
  * deployments.
  *
  * @param port The port that is to be used by this HTTP server. Typically, port 80 or 8080.
  * @param executor The executor that is used to handle HTTP exchanges, if <code>null</code>
  * 		a default strategy is used.
  *
  * @author Michael Eichberg
  * @author Mateusz Parzonka
  */
class JDKServer(protected[this] val server: HttpServer) extends CommonJDKServer[HttpServer] {

    def this(port: Int) {
        this(HttpServer.create(new InetSocketAddress(port), 0))
    }

    protected[this] val logger = Logger(classOf[JDKServer])

}

