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

import rest._
import log._
import utils._

import java.lang.Long
import java.net.{InetAddress,URI,URL}

class Time
        extends RESTInterface
        with ConsoleLogging // TODO needs to exchanged
        with PerformanceMonitor
        with TEXTSupport
        with HTMLSupport
        with XMLSupport {


    get returns TEXT {
        new java.util.Date().toString
    }

    get returns HTML {
        "<html><body>The current (server) time is: "+new java.util.Date().toString+"</body></html>"
    }

    get returns XML {
        <time>{ new java.util.Date().toString }</time>
    }
}

class User extends RESTInterface with TEXTSupport {

    var user : String = _

    get returns TEXT {
        "Welcome "+user
    }
}

/**
 * Implementation of a very primitive, thread-safe key-value store.
 */
object KVStore {

    private val ds = new scala.collection.mutable.HashMap[Long, String]()
    private var id = 0l

    private def nextId : Long = {
        id += 1l;
        id
    }

    def +(value : String) : Long = synchronized {
        val id : Long = nextId
        ds += ((id, value))
        id
    }

    def apply(id : Long) = synchronized {
        ds(id)
    }

    def size : Int = synchronized {
        ds.size
    }

    def keySet = synchronized {
        ds.keySet
    }

    def updated(id : Long, v : String) = synchronized {
        ds.update(id, v)
    }

    def contains(id : Long) = synchronized {
        ds.contains(id)
    }

    def remove(id : Long) = synchronized {
        ds.remove(id)
    }
}

class Keys extends RESTInterface with XMLSupport {

    get returns XML {
        KVStore.synchronized {
            <keys count={ KVStore.size.toString }>{
                for (k <- KVStore.keySet) yield <key>{ k }</key>
            }</keys>
        }
    }

    post of XML returns XML {
        val value = XMLRequestBody.text
        val id = KVStore + value

        // Set the response-header's Location field using the provided
        // convenience method: Location(URI)
        // Alternatively, it is possible to directly set the response headers
        // using the corresponding response headers data structure.
        Location( new URL("http://"+InetAddress.getLocalHost.getHostName+":9000/keys/"+id.toString) ) // TODO enable to specify the relative path

        // the "response body"
        <value id={ id.toString }>{ value }</value>
      }

}

class Key extends RESTInterface with XMLSupport {

    var id : Long = _

    get returns XML {
        KVStore.synchronized {
            if (!KVStore.contains(id)) {
                responseCode = 404 // 404 = NOT FOUND
                None // EMPTY BODY
            } else {
                val value = KVStore(id)
                <value id={ id.toString }>{ value }</value>
            }
        }
    }

    put of XML returns XML {
        KVStore.synchronized {
            if (!KVStore.contains(id)) {
                responseCode = 404 // NOT FOUND
                None
            } else {
                KVStore.updated(id, XMLRequestBody.text)
                <value id={ id.toString }>{
                    XMLRequestBody.text
                }</value>
            }
        }
    }

    delete {
        KVStore.remove(id).isDefined
    }

}

object Key {

    def setId(key : Key, id : Long) {
        key.id = id
    }
}

class MonitoredMappedDirectory(baseDirectory : String)
    extends MappedDirectory(baseDirectory)
    with ConsoleLogging // TODO needs to exchanged
    with PerformanceMonitor

class Demo

/**
 * To test the restful web serice you can use, e.g., curl. For example, to
 * add a value to the simple key-value store you can use:
 *
 * curl -v -X POST -d "<value>Test</value>" -H content-type:application/xml http://localhost:9009/keys
 * curl http://localhost:9009/keys
 */
object Demo
        extends JDKServer(9009)
        with scala.App
        with ConsoleLogging // TODO needs to exchanged
        {

    this register new HandlerFactory[Keys] {
        path {
            "/keys"
        }

        def create = new Keys
    }

    this register new HandlerFactory[Key] {
        path {
            // "/keys/" :: LongValue(v => _.id = v)

            //"/keys/" :: LongValue(Key.setId _)
            //"/keys/" :: LongValue((k,l) => k.id = l)
            "/keys/" :: LongValue(_.id = _)
        }

        def create = new Key
    }

    this register new HandlerFactory[User] {
        path {
            "/user/" :: StringValue(_.user = _)
        }

        def create = new User with PerformanceMonitor with ConsoleLogging // TODO needs to exchanged
    }

    register(
        new HandlerFactory[Time] {
            path {
                "/time" :: EmptyPath
            }
            query {
                NoQuery
            }

            /**
             * Reusing one instance of a resource to handle all requests requires that the resource is thread safe.
             * If you are unsure, just create a new instance for each request!
             *
             * If your resource is not trivially thread-safe, we recommend that you do not try to make it thread safe
             * and instead just create a new instance. (i.e., you don't write "lazy val" but write "def" in following.)
             *
             * In general, whenever you have to extract path parameters or have to process a request body or your
             * object representing the resource has some kind of mutable state, it is relatively certain that you
             * have to create a new instance to handle a request.
             */
            lazy val create = new Time() with PerformanceMonitor
        })

    // ("timezone",StringValue(v => _.timeZone = v))

    register(
        new HandlerFactory[MappedDirectory] {
            path {
                "/static" :: AnyPath(
                    v => _.path = {
                        if (v startsWith "/") v else "/"+v
                    })
            }

            def create = new MonitoredMappedDirectory(System.getProperty("user.home"))
        })

    // start the server
    start()
}




