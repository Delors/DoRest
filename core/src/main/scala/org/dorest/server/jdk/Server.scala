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

import com.sun.net.httpserver._
import java.net._
import log._

/**
 * Simple stand alone server that uses the (SUN) JDKs built-in HTTP server.
 *
 * @author Michael Eichberg
 * @author Mateusz Parzonka
 */
class Server(val port : Int)
        extends DoRestServer
        with DoRestApp 
        {
  
    private val logger = Logger(classOf[org.dorest.server.jdk.Server])

    private[this] val server = HttpServer.create(new InetSocketAddress(port), 0);

    private class DoRestHandler extends HttpHandler {
      
        def handle(t : HttpExchange) {

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
                var factories = Server.this.factories
                while (!factories.isEmpty) {
                    factories.head.matchURI(path, query) match {
                        case Some(handler) => {
                            handler.protocol = t.getProtocol()
                            handler.method = HTTPMethod(t.getRequestMethod())
                            handler.requestURI = t.getRequestURI()
                            handler.remoteAddress = t.getRemoteAddress().toString // TODO check that the result is as expected...
                            handler.localAddress = t.getLocalAddress().toString // TODO check that the result is as expected...
                            handler.requestHeaders = t.getRequestHeaders()
                            
                            // try to process a request and yield a response 
                            // (unpacking ResponseMappedExceptions to responses in case of throw)
                            val response: Response = {
                            try {
                                handler.processRequest(t.getRequestBody())
                            } catch {
                                case ex: RequestException =>  ex.response
                                }
                            }
                            
                            try {
                                response.body match {
                                    case Some(body) => {
                                        setContentTypeResponseHeader(response.headers, body)
                                        val length = body.length
                                        response.headers.foreach((header) => {
                                            val (key, value) = header;
                                            t.getResponseHeaders().set(key, value)
                                        })
                                        sendResponseHeaders(t, response.code, length);
                                        if (length > 0) {
                                            body.write(t.getResponseBody())
                                        }
                                    }
                                    case None => {
                                        response.headers.foreach((header) => {
                                            val (key, value) = header;
                                            t.getResponseHeaders().set(key, value)
                                        })
                                        sendResponseHeaders(t, response.code, -1);
                                    }
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
                        case _ => ; // the current handler factory's path didn't match the path
                    }
                    factories = factories.tail
                }
            } catch {
                // something went really wrong...
                case ex => {
                    logger.error(ex.toString())
                    sendResponseHeaders(t, 500, -1)
                    t.close()
                }
            }

            // does not match..
            sendResponseHeaders(t, 404, -1);
            t.close();
        }

    }

    protected def sendResponseHeaders(httpExchange : HttpExchange, code : Int, length : Int) {
        val it = httpExchange.getResponseHeaders.entrySet().iterator()
        while (it.hasNext) {
            val header = it.next()
            System.err.println(header.getKey+"="+header.getValue)
        }
        httpExchange.sendResponseHeaders(code, length)
    }

    /**
     * Starts the server.
     */
    def start() {
        server.createContext("/", new DoRestHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        logger.info( "JDKServer started at port: "+port )
    }
    
    /**
     * Stops the server after a given delay (seconds).
     */
    def stop(shutdownDelay: Int) {
       logger.info( "JDKServer at port %d has initiated shutdown. Will terminate in %d seconds.".format(port, shutdownDelay) )
       server.stop(shutdownDelay)
       logger.info( "JDKServer at port %d has terminated normally.".format(port) )
    }

}

