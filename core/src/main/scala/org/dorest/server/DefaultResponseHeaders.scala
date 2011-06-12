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


/**
 * Map based default implementation of the ResponseHeaders trait.
 *
 * @author Michael Eichberg
 */
class DefaultResponseHeaders(private var headers: Map[String, String] = Map())
        extends ResponseHeaders {

    def this(header: Tuple2[String, String]) {
        this (Map() + header)
    }

    def this(headers: List[Tuple2[String, String]]) {
        this (Map() ++ headers)
    }

    def set(key: String, value: String): Unit = {
        headers = headers.updated(key, value)
    }

    def foreach(f: ((String, String)) => Unit) {
        headers.foreach(f)
    }

}














