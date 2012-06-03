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

import org.dorest.server.auth.BasicAuthentication
import org.dorest.server.rest._
import org.dorest.server._

import log._
import utils._

/** After the start go to: "http://localhost:8080/time"
  */
object TimeServer extends JettyServer(8080) with URIsMatcher {

    addMatcher { // we match the entire path in one step
        case ("/time", _) ⇒ new Time() with PerformanceMonitor
    }

}

class Time
        extends RESTInterface
        with PerformanceMonitor
        with ConsoleLogging
        with TEXTSupport
        with HTMLSupport
        with XMLSupport {

    val dateString = new java.util.Date().toString

    get returns TEXT {
        dateString
    }

    get returns HTML {
        "<html><body>The current (server) time is: "+dateString+"</body></html>"
    }

    get returns XML {
        <time>
    		{ dateString }
    	</time>
    }
}

object MyApp extends scala.App {

    TimeServer

}

