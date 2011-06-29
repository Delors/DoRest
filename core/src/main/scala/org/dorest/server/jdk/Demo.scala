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
import java.lang.Long


class Time
        extends RESTInterface
                with PerformanceMonitor
                with TEXTSupport
                with HTMLSupport
                with XMLSupport {


    val dateString = new java.util.Date().toString

    get returns TEXT {
        dateString
    }


    get returns HTML {
        "<html><body>The current (server) time is: " + dateString + "</body></html>"
    }

    get returns XML {
        <time>{dateString}</time>
    }
}


class User extends RESTInterface with TEXTSupport {

    var user: String = _

    get returns TEXT {
        "Welcome " + user
    }
}


/**
 * Implementation of a very primitive, thread-safe key-value store.
 */
object KVStore {

    private val ds = new scala.collection.mutable.HashMap[Long, String]()
    private var id = 0l

    private def nextId: Long = {
        id += 1l;
        id
    }

    def +(value: String): Long = synchronized {
        val id: Long = nextId
        ds += ((id, value))
        id
    }

    def apply(id: Long) = synchronized {
        ds(id)
    }

    def size: Int = synchronized {
        ds.size
    }

    def keySet = synchronized {
        ds.keySet
    }

    def updated(id: Long, v: String) = synchronized {
        ds.update(id, v)
    }

    def contains(id: Long) = synchronized {
        ds.contains(id)
    }

    def remove(id: Long) = synchronized {
        ds.remove(id)
    }
}

class Keys extends RESTInterface with XMLSupport {

    get returns XML {
        KVStore.synchronized {
            <keys count={"" + KVStore.size}>{
                for (k <- KVStore.keySet) yield <key>{k}</key>
            }</keys>
        }
    }

    post of XML returns XML {
        val value = XMLRequestBody.text
        val id = KVStore + value
        <value id={"" + id}>{value}</value>
    }

}

class Key extends RESTInterface with XMLSupport {

    var id: Long = _

    get returns XML {
        KVStore.synchronized {
            if (!KVStore.contains(id)) {
                responseCode = 404 // NOT FOUND
                None
            } else {
                val value = KVStore(id)
                <value id={"" + id}>
                    {value}
                </value>
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
                <value id={"" + id}>
                    {XMLRequestBody.text}
                </value>
            }
        }
    }

    delete {
        KVStore.remove(id).isDefined
    }

}

object Key {

    def setId(key: Key,id: Long) {
        key.id = id
    }
}


class MonitoredMappedDirectory(baseDirectory: String)
        extends MappedDirectory(baseDirectory)
                with PerformanceMonitor


class Demo

object Demo extends Server(9000) with App {

    this register new HandlerFactory[Keys] {
        path {
            "/keys"
        }

        /**
         * Reusing one instance of a Resource to handle all requests requires that the resource is thread safe.
         * If you are unsure, just create a new instance for each request!
         * If your resource is not trivially thread-safe, we recommend that you do not try to make it thread safe
         * and instead just create a new instance. (i.e., you don't write "lazy val" but write "def" in following.)
         */
        lazy val create = new Keys
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
            "/user/" :: StringValue(v => _.user = v)
        }

        def create = new User with PerformanceMonitor
    }

    register(
        new HandlerFactory[Time] {
            path {
                "/time" :: EmptyPath
            }
            query {
                NoQuery
            }

            // ("timezone",StringValue(v => _.timeZone = v))
            def create = new Time() with PerformanceMonitor
        })

    register(
        new HandlerFactory[MappedDirectory] {
            path {
                "/static" :: AnyPath(
                    v => _.path = {
                        if (v startsWith "/") v else "/" + v
                    })
            }

            def create = new MonitoredMappedDirectory(System.getProperty("user.home"))
        })


    start()
}




