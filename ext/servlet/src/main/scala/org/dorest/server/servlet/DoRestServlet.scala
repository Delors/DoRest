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
package servlet

import javax.servlet.http._
import org.dorest.server._


/**
 * After the start go to: "http://localhost:8080/date"
 */
class DoRestServlet extends javax.servlet.http.HttpServlet with DoRestServer {

    override def service(req: HttpServletRequest, res: HttpServletResponse) {

        val path = req.getRequestURI
        val query = req.getQueryString

        println("Handling request at " + new java.util.Date + " :" + path)

        var factories = MyApp.factories
        while (!factories.isEmpty) {
            factories.head.matchURI(path, query) match {
                case Some(_handler) => {
                    val handler = _handler.asInstanceOf[Handler]
                    handler.protocol = req.getProtocol()
                    handler.method = HTTPMethod(req.getMethod())
                    handler.requestURI = new java.net.URI(req.getRequestURI())
                    handler.remoteAddress = req.getRemoteAddr()
                    handler.localAddress = req.getLocalAddr()
                    handler.requestHeaders = new Object {
                        def getFirst(key: String): String = {
                            req.getHeader(key)
                        }
                    }

                    val response = handler.processRequest(req.getInputStream)
                    try {
                        res.setStatus(response.code);
                        response.body match {
                            case Some(body) => {
                                setContentTypeResponseHeader(response.headers, body)
                                response.headers.foreach((header) => {
                                    val (key, value) = header;
                                    res.setHeader(key, value)
                                }
                                )
                                body.write(res.getOutputStream())
                            }
                            case None =>
                                response.headers.foreach((header) => {
                                    val (key, value) = header;
                                    res.setHeader(key, value)
                                } )
                        }


                    } catch {
                        case ex => {
                            ex.printStackTrace()
                        }
                    } finally {
                        // we were able to handle the request..
                        return;
                    }
                }
                case _ =>;
            }
            factories = factories.tail
        }
        res.sendError(404)
    }

}
