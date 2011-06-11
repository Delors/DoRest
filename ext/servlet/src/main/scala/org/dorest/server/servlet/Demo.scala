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

import org.dorest.server.rest._
import org.dorest.server._
import auth.{SimpleAuthenticator, BasicAuthentication}


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


object MyApp {

    var factories: scala.collection.immutable.List[HandlerFactory[_]] = Nil

    def register(handlerFactory: HandlerFactory[_]) {
        factories = factories.:+(handlerFactory)
    }

    register(new HandlerFactory[Time] {
        path {
            "/time"
        }

        def create = new Time() with PerformanceMonitor
    })


}

class Time
        extends RESTInterface
        with PerformanceMonitor
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

