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
import scala.collection.mutable.HashMap
import scala.collection.mutable.MultiMap
import scala.collection.mutable.Set

/**
 * Map based implementation of the ResponseHeaders trait.
 *
 * @author Michael Eichberg
 */
class DefaultResponseHeaders(private var headers: MultiMap[String, String] = new HashMap[String, Set[String]] with MultiMap[String, String]) extends ResponseHeaders {

    def this(header: Tuple2[String, String]) {
        this()
        headers.addBinding(header._1, header._2)
    }

    def this(headers: List[Tuple2[String, String]]) {
        this()
        headers.foreach { case (first, second) => this.headers.addBinding(first, second) }
    }

    def set(field: String, value: String): this.type = {
        headers.addBinding(field, value)
        this
    }

    def foreach[U](f: ((String, String)) ⇒ U) {
        headers.foreach { case (key, values) => values.foreach(value => f(key, value)) }
    }

    def apply(field: String): Set[String] = {
        headers(field)
    }

    def get(field: String): Option[Set[String]] = {
        headers.get(field)
    }
    
    def remove(field:String)={
        headers.remove(field)
        this
    }
}

object DefaultResponseHeaders {

    def apply(headers: (String, String)*): DefaultResponseHeaders = new DefaultResponseHeaders(headers.toList)

}

