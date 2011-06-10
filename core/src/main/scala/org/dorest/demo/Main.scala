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
package org.dorest.demo

import org.dorest.server._
import org.dorest.server.rest._

class Main

object Main extends Server(9000) with App {
    /*
    this register new HandlerFactory[User] {
        path { "/user/" :: StringValue((v) => _.user = v) }
        def create = new User with MonitoringHandler
    }*/

    register(new HandlerFactory[Time] {
        path {
            "/time" :: EmptyPath
        }
        query {
            NoQuery
        }

        // ("timezone",StringValue(v => _.timeZone = v))
        def create = new Time() with MonitoringHandler
    })

    /*  register(new HandlerFactory[Tags] {
            path { "/tags" :: Optional("/") }
            def create = new Tags
        })

        register(new HandlerFactory[Tag] {
            path { "/tags/" :: LongValue((v) => (r) => r.tagId = v) }
            def create = new Tag
        })
    */
    register(new HandlerFactory[MappedDirectory] {
        path {
            "/webapp" :: AnyPath(v => _.path = {
                if (v startsWith "/") v else "/" + v
            })
        }

        def create = new MonitoredMappedDirectory("/Users/Michael")
    })

    /*    register(new HandlerFactory[Note] {
            path { "/tags/" :: LongValue((v) => _.tagId = v) :: "/notes/" :: LongValue((v) => _.noteId = v) }
            def create = new Note
        })
    */
    start()
}

/*
trait Authorization
        extends BasicAuthentication
        with SimpleAuthenticator
        with AuthenticatedUser {

    def authenticationRealm = "Demo App"
    val authorizationUser = "user"
    val authorizationPwd = "safe"
}

class Tags
        extends RESTInterface
        with MonitoringHandler
        with JSONSupport
        with Authorization {

    get requests JSON {
        val ja = new JSONArray()
        var i = 0
        while (i < 10) {
            val jo = new JSONObject()
            jo.put("t_id", i)
            ja.put(jo)
            i += 1
        }
        ja
    }

}

class Tag
        extends RESTInterface
        with JSONSupport
        with Authorization {

    var tagId: Long = 0

    get requests JSON {
        val jo = new JSONObject()
        jo.put("authenticatedUser", authenticatedUser)
        jo.put("t_id", tagId)
        jo
    }

}

class Note extends RESTInterface with JSONSupport {

    var tagId: Long = _

    var noteId: Long = _

    get requests JSON {
        val jo = new JSONObject()
        jo.put("t_id", tagId)
        jo.put("n_id", noteId)
        jo
    }
}

class User() extends RESTInterface with JSONSupport {

    var user: String = _

    get requests JSON {
        val jo = new JSONObject()
        jo.put("user", user)
        jo
    }
}
*/

class Time
        extends RESTInterface
        with MonitoringHandler
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


class MonitoredMappedDirectory(baseDirectory: String)
        extends MappedDirectory(baseDirectory)
        with MonitoringHandler

