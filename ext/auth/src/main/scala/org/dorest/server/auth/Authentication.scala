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
  * Enables validation of user credentials.
  *
  * @see [[http://doi.acm.org/10.1145/2246036.2254400 Poul-Henning Kamp. 2012. LinkedIn Password Leak: Salt
  * Their Hide. Queue 10, 6, Pages 20 (June 2012), 3 pages. DOI=10.1145/2246036.2254400 ]] for information
  * regarding hashing/storing passwords.
  *
  * @author Mateusz Parzonka
  */
trait Authentication {

    /**
      * Provides the authentication realm to be included in an "unauthorized"-response.
      */
    def authenticationRealm: String

    /**
      * Returns the password for a given user name (if available).
      *
      * '''Control Flow''': This method is called by the Basic/DigestAuthentication traits when a user tries
      * to log in. The handler then has to look up the password of the provided user.
      */
    def password(username: String): Option[String]

    /**
      * The name of the (successfully) authenticated user.
      */
    def authenticatedUser: String

}
