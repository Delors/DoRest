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
package auth

import org.dorest.server.jdk._
import org.dorest.server._
import org.dorest.server.rest._


/**
 * Demonstrates how to get http basic authentication.
 *
 * If you require the authentication of the user against, e.g., a database just mix in an appropriate trait instead
 * of the {{{SimpleAuthenticator}}} trait.
 */
trait Authorization
        extends BasicAuthentication
        with SimpleAuthenticator
        with AuthenticatedUser {

    def authenticationRealm = "Demo App"

    val authorizationUser = "user"
    val authorizationPwd = "safe"
}


class Time
        extends RESTInterface
        with Authorization
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

class Demo

object Demo extends Server(9000) with App {


    register(new HandlerFactory[Time] {
        path {
            "/time"
        }

        def create = new Time()
    })


    register(new HandlerFactory[MappedDirectory] {
        path {
            "/static" :: AnyPath(v => _.path = {
                if (v startsWith "/") v else "/" + v
            })
        }

        def create = new MappedDirectory(System.getProperty("user.home")) with Authorization
    })

    start()
}


