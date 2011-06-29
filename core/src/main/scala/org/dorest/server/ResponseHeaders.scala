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
 * A response's headers.
 *
 * @author Michael Eichberg
 */
trait ResponseHeaders extends collection.Traversable[Tuple2[String,String]] {

    /**
     * Sets the value of the specified response header.
     *
     * Cf. <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14">HTTP Header Fields</a>.
     */
    def set(key: String, value: String): Unit

    /**
     * Enables you to iterate over all response headers.
     */
    def foreach[U](f: ((String, String)) => U): Unit
}





























