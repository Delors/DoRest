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

/** @author Michael Eichberg
  * @author Mateusz Parzonka
  */
trait CommonJDKServer[S <: HttpServer] extends DoRestServer with DoRestApp with URIsMatcher {

    protected[this] def logger: Logger

    protected[this] def server: S

    protected[this] class DoRestHandler extends HttpHandler {

        def handle(t: HttpExchange) {

            val uri = t.getRequestURI.normalize()
            val path = uri.getPath
            val query = uri.getQuery

            logger.info {
                var message = ""+t.getRemoteAddress()+" "+new java.util.Date
                message += path+"?"+query
                val it = t.getRequestHeaders().entrySet.iterator
                while (it.hasNext) {
                    message += "; "+it.next
                }
                message
            }

            try {
                var factories = CommonJDKServer.this.factories
                while (!factories.isEmpty) {
                    factories.head.matchURI(path, query) match {
                        case Some(handler) ⇒ {
                            handler.protocol = t.getProtocol()
                            handler.method = HTTPMethod(t.getRequestMethod()).get // TODO What do we want to do if the method is completely unknown 
                            handler.requestURI = t.getRequestURI()
                            handler.remoteAddress = t.getRemoteAddress().toString // TODO check that the result is as expected...
                            handler.localAddress = t.getLocalAddress().toString // TODO check that the result is as expected...
                            handler.requestHeaders = t.getRequestHeaders()

                            // try to process a request and yield a response
                            // (unpack RequestException to response in case of throw)
                            val response: Response = {
                                try {
                                    handler.processRequest(t.getRequestBody())
                                }
                                catch {
                                    case ex: RequestException ⇒ ex.response
                                }
                            }

                            try {
                                response.body match {
                                    case Some(body) ⇒ {
                                        setContentTypeResponseHeader(response.headers, body)
                                        val length = body.length
                                        response.headers.foreach((header) ⇒ {
                                            val (key, value) = header;
                                            t.getResponseHeaders().set(key, value)
                                        })
                                        sendResponseHeaders(t, response.code, length);
                                        if (length > 0) {
                                            body.write(t.getResponseBody())
                                        }
                                    }
                                    case None ⇒ {
                                        response.headers.foreach((header) ⇒ {
                                            val (key, value) = header;
                                            t.getResponseHeaders().set(key, value)
                                        })
                                        sendResponseHeaders(t, response.code, -1);
                                    }
                                }
                                t.close();
                            }
                            catch {
                                case ex ⇒ {
                                    ex.printStackTrace()
                                }
                            }
                            finally {
                                // we were able to handle the request..
                                return ;
                            }
                        }
                        case _ ⇒ ; // the current handler factory's path didn't match the path
                    }
                    factories = factories.tail
                }
            }
            catch {
                // something went really wrong...
                case ex ⇒ {
                    logger.error(ex.toString())
                    ex.printStackTrace()
                    sendResponseHeaders(t, 500, -1)
                    t.close()
                }
            }

            // does not match..
            sendResponseHeaders(t, 404, -1);
            t.close();
        }

    }

    protected def sendResponseHeaders(httpExchange: HttpExchange, code: Int, length: Int) {
        val it = httpExchange.getResponseHeaders.entrySet().iterator()
        while (it.hasNext) {
            val header = it.next()
            System.err.println(header.getKey+"="+header.getValue)
        }
        httpExchange.sendResponseHeaders(code, length)
    }

    /** Starts the server.
      */
    def start(executor: Executor = Executors.newCachedThreadPool()) {
        server.createContext("/", new DoRestHandler());
        server.setExecutor(executor); // creates a default executor
        server.start();
        logger.info("JDKServer started: "+server.getAddress())
    }

    /** Stops the server after a given delay (seconds).
      */
    def stop(shutdownDelay: Int) {
        logger.info("JDKServer "+server.getAddress()+" has initiated shutdown. Will terminate in %d seconds.".format(shutdownDelay))
        server.stop(shutdownDelay)
        logger.info("JDKServer "+server.getAddress()+" has terminated normally.")
    }

}

