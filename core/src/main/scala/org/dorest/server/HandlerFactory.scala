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

import java.lang.Long

/**
 * A HandlerFactory is responsible for matching URIs and – if an URI matches – to return a [[org.dorest.server.Handler]]
 * that will then be used to handle the request and to create the response.
 *
 * '''Thread Safety''': Handler factories have to be thread safe. I.e., handler factories have to support the
 * simultaneous matching of URIs; the DoRest framework use a single Handler factory for matching URIs. However,
 * a Handler object has to be thread safe if and only if the same handler object is returned more than once.
 * Hence, to avoid/limit concurrency issues it is recommended to return a new Handler whenever a path matches.
 *
 * @author Michael Eichberg
 */
trait HandlerFactory {

    /**
     * Tries to match the path and query part of a given URI.
     *
     * '''Control Flow''': This method is called by the DoRest framework when an HTTP request is made.
     * For example, imagine an HTTP request with the following
     * URI is made: `http://www.opal-project.de/pages?search=DoRest`. In this case DoRest will analyze the URI
     * and split it up. The path part would be `/pages` and the query string would be `search=DoRest`.
     *
     * @param path A URI's path part.
     * @param query A URI's query part.
     * @return `Some` handler if the path and query were successfully matched. `None` otherwise.
     */
    def matchURI(path: String, query: String): Option[Handler]

}

