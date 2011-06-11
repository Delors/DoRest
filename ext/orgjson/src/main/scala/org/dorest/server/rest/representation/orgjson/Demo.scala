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
package org.dorest.server.rest.representation.orgjson

import org.dorest.server._
import org.dorest.server.jdk._
import org.dorest.server.rest._
import org.json._

/**
 * A simple service that just echos the received JSON object.
 */
class Echo
        extends RESTInterface
        with ORGJSONSupport {

    get requests JSON {
        val jo = new JSONObject()
        jo.put("received", "")
        jo
    }

    // JSONRequestBody is an org.JSONObject
    post receives JSON returns JSON {
        // JSONRequestBody // if we just want return what we have received

        val jo = new JSONObject()
        jo.put("received", JSONRequestBody)
        jo
    }

}

/**
 * A resource that returns the current (server-side) time.
 */
class Time
        extends RESTInterface
        with ORGJSONSupport
        with TEXTSupport {

    val dateString = new java.util.Date().toString

    get requests JSON {
        val jo = new JSONObject()
        jo.put("time", dateString)
        jo
    }

    get requests TEXT {
        dateString
    }


}


/**
 * Demonstrates how to use the org.json support for processing requests.
 *
 * The available services/resources are:
 * {{{http://localhost:9000/echo}}}
 * A POST (Accept: application/json Content-Type: application/json Body: {"message":"this is cool"} will
 * return :
 * {{{
 * HTTP/1.1 200 OK
 * Content-Length: 39
 * {"received":{"message":"this is cool"}}
 * }}}
 *
 * {{{http://localhost:9000/time}}}
 * A GET will return, e.g.:
 * {{{
 * HTTP/1.1 200 OK
 * Content-Type: application/json; charset=UTF-8
 * Content-Length: 40
 *
 * {"time":"Sat Jun 11 13:52:17 CEST 2011"}
 * }}}
 * @see [[[org.dorest.server.rest.representation.orgjson]]]
 * @author Michael Eichberg
 */
class Demo

object Demo extends Server(9000) with App {

    register(new HandlerFactory[Echo] {
        path {
            "/echo"
        }

        def create = new Echo()
    })

    register(new HandlerFactory[Time] {
        path {
            "/time"
        }

        def create = new Time() with PerformanceMonitor
    })

    start()
}


