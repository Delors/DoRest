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
package rest.representation.gson

import log._
import utils._
import jdk._
import rest._

import com.google.gson._

case class SubDomain(var value: Int)

case class DemoDomain(var value: String, var subDomain: SubDomain) {

}

object DemoStore {

    val store = scala.collection.mutable.ListBuffer[DemoDomain]()

}

class DemoResource
        extends RESTInterface
        with GSONSupport {

    type DomainType = DemoDomain
    def domainClass: java.lang.Class[DomainType] = classOf[DemoDomain]

    get returns JSON {
        DemoDomain.apply("Benjamin",  SubDomain(1))
    }
}

/**
 * A resource that returns the current (server-side) time.
 */
class DemosResource
        extends RESTInterface
        with GSONSupport {

    type DomainType = DemoDomain
    def domainClass: java.lang.Class[DomainType] = classOf[DemoDomain]

    post of JSON returns JSON {
        DemoStore.store += JSONRequestBody
        JSONRequestBody
    }

    get returns JSON {
        DemoStore.store
        // None
    }
}

/**
 * @author Michael Eichberg
 */
class Demo

object Demo extends JDKServer(9000) with App {

    register(new HandlerFactory[DemosResource] {
        path { "/demos" }

        def create = new DemosResource()

    })

    //    register(new HandlerFactory[Time] {
    //
    //        path { "/time" }
    //
    //        def create = new Time() with PerformanceMonitor with ConsoleLogging
    //
    //    })
    //
    start()
}


