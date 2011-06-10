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
package auth

/**
 * Trait that can be mixed in if the user name of an authenticated user is
 * required later on.
 */
trait AuthenticatedUser extends Authentication {

    /**
     * Stores the name of a successfully authenticated user.
     */
    var authenticatedUser : String = null

    abstract override def authenticate(user : String, pwd : String) : Boolean = {
        val authenticated = super.authenticate(user, pwd)
        if (authenticated) {
            authenticatedUser = user
        }
        authenticated
    }

}