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

import org.dorest.server.rest._

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

class Tags extends RESTInterface with PerformanceMonitor with TEXTSupport {

    get requests TEXT {
        "tagIds = a,b,c"
    }

}

class Tag extends RESTInterface with TEXTSupport {

    var tagId: Long = _

    get requests TEXT {
        "tagId = " + tagId
    }

}

class Note extends RESTInterface with TEXTSupport {

    var tagId: Long = _

    var noteId: Long = _

    get requests TEXT {
        "tagId = " + tagId + " noteId = " + noteId
    }
}


class User extends RESTInterface with TEXTSupport {

    var user: String = _

    get requests TEXT {
        "Welcome " + user
    }
}


class MonitoredMappedDirectory(baseDirectory: String)
        extends MappedDirectory(baseDirectory)
        with PerformanceMonitor


class Demo

object Demo extends Server(9000) with App {


    this register new HandlerFactory[User] {
        path {
            "/user/" :: StringValue((v) => _.user = v)
        }

        def create = new User with PerformanceMonitor
    }

    register(new HandlerFactory[Time] {
        path {
            "/time" :: EmptyPath
        }
        query {
            NoQuery
        }

        // ("timezone",StringValue(v => _.timeZone = v))
        def create = new Time() with PerformanceMonitor
    })

    register(new HandlerFactory[Note] {
        path {
            "/tags/" :: LongValue((v) => _.tagId = v) :: "/notes/" :: LongValue((v) => _.noteId = v)
        }

        def create = new Note
    })

    register(new HandlerFactory[Tags] {
        path {
            "/tags" :: Optional("/")
        }

        def create = new Tags
    })

    register(new HandlerFactory[Tag] {
        path {
            "/tags/" :: LongValue((v) => (r) => r.tagId = v)
        }

        def create = new Tag
    })

    register(new HandlerFactory[MappedDirectory] {
        path {
            "/static" :: AnyPath(v => _.path = {
                if (v startsWith "/") v else "/" + v
            })
        }

        def create = new MonitoredMappedDirectory(System.getProperty("user.home"))
    })


    start()
}




