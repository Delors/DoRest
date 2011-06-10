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

//import auth._

import com.sun.net.httpserver._
import com.sun.net.httpserver.HttpServer._
import java.net._

// TODO implement something like a server adapter...
class Server(val port: Int) {

    private var factories: List[HandlerFactory[_ <: Handler]] = Nil

    private val server = HttpServer.create(new InetSocketAddress(port), 0);

    private class DefaultHandler extends HttpHandler {

        def handle(t: HttpExchange) {

            val uri = t.getRequestURI.normalize()
            val path = uri.getPath
            val query = uri.getQuery

            println("Handling request at "+new java.util.Date+" :"+path)

            val it = t.getRequestHeaders().entrySet.iterator
            while (it.hasNext)
                println(it.next)

            try {
                var factories = Server.this.factories
                while (!factories.isEmpty) {
                    factories.head.matchURI(path, query) match {
                        case Some(handler) => {
                            handler.protocol = t.getProtocol()
                            handler.method = HTTPMethod.withName(t.getRequestMethod())
                            handler.requestURI = t.getRequestURI()
                            handler.remoteAddress = t.getRemoteAddress().toString // TODO check that the result is as expected...
                            handler.localAddress = t.getLocalAddress().toString // TODO check that the result is as expected...
                            handler.requestHeaders = t.getRequestHeaders()
                            val response = handler.processRequest(t.getRequestBody())
                            try {
                                val length = response.body.length
                                response.headers.foreach((header) => { val (key, value) = header; t.getResponseHeaders().set(key, value) })
                                t.sendResponseHeaders(response.code, length);
                                if (length > 0) {
                                    response.body.write(t.getResponseBody())
                                }
                                t.close();
                            } catch {
                                case ex => {
                                    ex.printStackTrace()
                                }
                            } finally {
                                // we were able to handle the request..
                                return ;
                            }
                        }
                        case _ => ;
                    }
                    factories = factories.tail
                }
            } catch {
                // something went really wrong...
                case ex => {
                    ex.printStackTrace();
                    t.sendResponseHeaders(500, 0);
                    t.close();
                }
            }

            // does not match..
            t.sendResponseHeaders(404, 0);
            t.close();
        }

    }

    def start() {
        server.createContext("/", new DefaultHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        println("Server started...: "+port)
    }

    def register(handlerFactory: HandlerFactory[_ <: Handler]) {
        factories = factories.:+(handlerFactory)
    }

}


