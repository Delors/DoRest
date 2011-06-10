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

import java.util.ArrayList;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.ServletMapping;

import javax.servlet.http._
import org.dorest.server.rest._
import org.dorest.server._
import auth.{SimpleAuthenticator, BasicAuthentication}
import org.json.JSONObject

/**
 * After the start go to: "http://localhost:8080/date"
 */
class JettyServer

object JettyServer extends App {

    // all Jetty objects required to start
    val connector = new SelectChannelConnector();
    val server = new Server();
    val mainHandler = new HandlerList();
    val port = 8080;
    val servletHandler = new ServletHandler();

    // local lists storing the servlets
    val holders = new ArrayList[ServletHolder]();
    val mappings = new ArrayList[ServletMapping]();

    // protocol bits
    var initAlreadyCalled = false;

    /**Initializes the server */
    def init() {
        if (initAlreadyCalled) {
            throw new RuntimeException("init() already called");
        }

        connector.setPort(port);
        server.setConnectors(Array.apply[Connector](connector));

        val resourceHandler = new ResourceHandler();
        resourceHandler.setResourceBase("./");

        // recall that Jetty takes into account
        // the first handler which matches the requested URI    
        mainHandler.addHandler(resourceHandler);
        mainHandler.addHandler(servletHandler);
        server.setHandler(mainHandler);

        initAlreadyCalled = true;
    }

    /**Starts the server */
    def start() {
        if (!initAlreadyCalled) {
            throw new RuntimeException("init() must be called before start()");
        }
        servletHandler.setServlets(holders.toArray(new Array[ServletHolder](0)));
        servletHandler.setServletMappings(mappings.toArray(new Array[ServletMapping](0)));
        server.start();
    }

    def registerServlet(servlet: java.lang.Class[_], path: String) {
        if (!initAlreadyCalled) {
            throw new RuntimeException("init() must be called before registerServlet()");
        }
        val holder = new ServletHolder();
        holder.setName(servlet.getName());
        holder.setClassName(servlet.getName());
        holders.add(holder);
        val mapping = new ServletMapping();
        mapping.setPathSpec(path);
        mapping.setServletName(servlet.getName());
        mappings.add(mapping);
    }

    init();
    registerServlet(classOf[DoRestServlet], "/");
    start();

}

class DoRestServlet extends javax.servlet.http.HttpServlet {

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
                    handler.method = HTTPMethod.withName(req.getMethod())
                    handler.requestURI = new java.net.URI(req.getRequestURI())
                    handler.remoteAddress = req.getRemoteAddr()
                    handler.localAddress = req.getLocalAddr()
                    handler.requestHeaders = new Object {
                        def getFirst(key: String): String = {
                            req.getHeader(key)
                        }
                    }

                    req.getInputStream
                    val response = handler.processRequest(req.getInputStream())
                    try {
                        val length = response.body.length
                        response.headers.foreach((header) => {
                            val (key, value) = header;
                            res.setHeader(key, value)
                        }
                        )
                        res.setStatus(response.code);
                        if (length > 0) {
                            response.body.write(res.getOutputStream())
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

object MyApp {

    var factories: scala.collection.immutable.List[HandlerFactory[_]] = Nil

    def register(handlerFactory: HandlerFactory[_]) {
        factories = factories.:+(handlerFactory)
    }

    register(new HandlerFactory[Time] {
        path {"/time" :: EmptyPath}

        def create = new Time() with MonitoringHandler
    })

    register(new HandlerFactory[Echo] {
        path {"/echo" :: EmptyPath}

        def create = new Echo()
    })


}

class Time
        extends RESTInterface
                with MonitoringHandler
                //with JSONSupport
                with TEXTSupport
                with HTMLSupport
                with XMLSupport {

    val dateString = new java.util.Date().toString

    get requests TEXT {
        dateString
    }

    get requests HTML {
        "<html><body>The current (server) time is: " + dateString + "</body></html>"
    }

    get requests XML {
        <time>
            {dateString}
        </time>
    }
}


class Echo extends RESTInterface with JSONSupport with BasicAuthentication
                   with SimpleAuthenticator {

    def authenticationRealm = "Demo App"

    val authorizationUser = "user"
    val authorizationPwd = "safe"


    get requests JSON {
        val jo = new JSONObject()
        jo.put("message", "You have to post something to get something :-)")
        jo
    }

    post receives JSON returns JSON {
        // just mirror the request
        JSONRequestBody
    }

}
