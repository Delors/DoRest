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

import java.util.ArrayList

import org.dorest.server.HandlerFactory
import org.dorest.server.DoRestServer
import org.dorest.server.log.Logger
import org.eclipse.jetty.server.handler.HandlerList
import org.eclipse.jetty.server.handler.ResourceHandler
import org.eclipse.jetty.server.nio.SelectChannelConnector
import org.eclipse.jetty.server.Connector
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.eclipse.jetty.servlet.ServletMapping

import log.Logging

/**
 * 
 * @author Michael Eichberg
 * @author Mateusz Parzonka
 */
class JettyServer(val port: Int) extends DoRestServer {

  private val logger = Logger("org.dorest.server.servlet.JettyServer")

  // all Jetty objects required to start
  val connector = new SelectChannelConnector();
  val server = new Server();
  val mainHandler = new HandlerList();
  val servletHandler = new ServletHandler();

  // local lists storing the servlets
  val holders = new ArrayList[ServletHolder]();
  val mappings = new ArrayList[ServletMapping]();

  // protocol bits
  var initAlreadyCalled = false;

  init();
  registerServlet(classOf[DoRestServlet], "/");
  start();

  /**Initializes the server */
  private def init() {
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
    logger.info("Jetty started.")
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

  /**
   * Delegated to the global registry.
   */
  def register(handlerFactory: HandlerFactory[_ <: Handler]) {
    DoRestServlet.register(handlerFactory)
  }

  /**
   * Stops the server after the given time (seconds).
   */
  def stop(shutdownDelay: Integer) {
    logger.info("Stopping Jetty in in %s seconds...".format(shutdownDelay))
    new Thread(
      new Runnable() {
        def run() {
          Thread.sleep(shutdownDelay * 1000)
          server.stop();
          logger.info("Stopped.")
        }
      }).start
  }

}
