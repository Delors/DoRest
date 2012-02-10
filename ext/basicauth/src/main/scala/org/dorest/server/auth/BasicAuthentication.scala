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

import java.io.InputStream

/**
 * Implements <a href="http://www.ietf.org/rfc/rfc1945.txt.pdf">HTTP basic access authentication</a>.
 *
 * @see [[org.dorest.server.auth.SimpleAuthenticator]]
 * @author Michael Eichberg
 * @author Mateusz Parzonka
 */
trait BasicAuthentication extends Authentication with Handler {

  private[this] var _authenticatedUser: String = _

  def authenticatedUser: String = _authenticatedUser

  override abstract def processRequest(requestBody: => InputStream): Response = {

    def parseAuthorizationHeader(authorizationHeader: String): Array[String] =
      new String(javax.xml.bind.DatatypeConverter.parseBase64Binary(authorizationHeader.substring("Basic ".length))).split(":")

    requestHeaders.getFirst("Authorization") match {
      case authorizationHeader: String if authorizationHeader.startsWith("Basic ") =>
        {
          parseAuthorizationHeader(authorizationHeader) match {
            case Array(username: String, requestPassword: String) => password(username) match {
              case Some(validPassword) if requestPassword == validPassword => { _authenticatedUser = username; super.processRequest(requestBody) }
              case _ => UnauthorizedBasicResponse(authenticationRealm)
            }
            case _ => BadRequest()
          }
        }
      case _ => UnauthorizedBasicResponse(authenticationRealm)
    }
  }
}
