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
package org.dorest.server.auth

/**
 * Enables the authentication of a specific user.
 *
 * @author Michael Eichberg
 */
trait Authentication {

    /**
     * Tries to authenticate the given user. If the authentication
     * fails, <code>false</code> has to be returned.<p>
     * If the user credentials, in particular the user name,
     * is later on required, it is possible to additionally use, e.g.,
     * the trait [[org.dorest.server.auth.AuthenticatedUser]].
     */
    def authenticate(user: String, pwd: String): Boolean
}
