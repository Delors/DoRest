package helloworld

import org.dorest.server.jdk.JDKServer
import org.dorest.server.rest.HTMLSupport
import org.dorest.server.rest.RESTInterface
import org.dorest.server.HandlerFactory

// server object
object HelloWorldServer extends JDKServer(9000) with App {

   // registering the handler
  this register new HandlerFactory[HelloWorldHandler] {
    path { "/hello" }
    def create = new HelloWorldHandler
  }
  
  // handler
  class HelloWorldHandler extends RESTInterface with HTMLSupport {
    get returns HTML { "<html><body><h1>Hello World!</h1></body></html>" }
  }

  start()

}
