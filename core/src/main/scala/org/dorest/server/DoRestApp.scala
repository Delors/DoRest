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
 * Base trait of all DoRest applications. It enables the registration of [[org.dorest.server.HandlerFactory]] objects
 * and provides the functionality to select matching handlers.
 *
 * @author Michael Eichberg
 */
trait DoRestApp {

    // TODO Should we use another data structure that enables efficient appending of handler factories, if we have more than one? How many handlers do we usually have?
    private var _factories: List[HandlerFactory[_ <: Handler]] = Nil

    def factories = _factories

    def register(handlerFactory: HandlerFactory[_ <: Handler]) {
        // we need to append...
        _factories = _factories.:+(handlerFactory)
    }


}




