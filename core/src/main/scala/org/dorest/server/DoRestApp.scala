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

import collection.mutable.Buffer

/**
 * Enables the registration of [[org.dorest.server.HandlerFactory]] objects.  When processing a request the server
 * will will use/has to use the first HandlerFactory that returns a Handler object to process the request.
 * The factories are/have to be tried in the order in which they are registered using the register method.
 *
 * This trait is generally implemented by DoRest servers.
 *
 * @author Michael Eichberg
 */
trait DoRestApp {

    /**
     * The list of all registered ˚HandlerFactory˚ objects.
     */
    protected[this] var factories = Buffer[HandlerFactory]()

    /**
     * Appends the given handler factory to the list of previously registered `HandlerFactory` objects.
     */
    def register(handlerFactory: HandlerFactory) {
        factories += handlerFactory
    }

}
