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
package helloworld

import org.dorest.server.jdk.JDKServer
import org.dorest.server.rest.RESTInterface
import org.dorest.server.rest.TEXTSupport
import org.dorest.server.HandlerFactory

// server object
object HelloWorldServer extends JDKServer(9000) with App {

   // registering the handler
  this register new HandlerFactory[HelloWorldHandler] {
    path { "/hello" }
    def create = new HelloWorldHandler
  }
  
  // handler
  class HelloWorldHandler extends RESTInterface with TEXTSupport {
    get returns TEXT { "Hello World!" }
  }

  start()

}
