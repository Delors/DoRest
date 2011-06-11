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
 */
trait BasicAuthentication extends Authentication with Handler {

    /**
     * The required authentication realm, e.g. "My Website", that is sent back if
     * the request does not contain the necessary credentials.
     */
    def authenticationRealm : String

    override abstract def processRequest(requestBody : InputStream) : Response = {
        var authorizationInfo = requestHeaders.getFirst("Authorization")
        if (authorizationInfo == null) {
            return Unauthorized(
                "Authorization required.",
                "Basic realm=\""+authenticationRealm+"\""
            )
        }

        if (!authorizationInfo.startsWith("Basic ")) {
            return BadRequest
        }

        authorizationInfo = authorizationInfo.substring("Basic ".length)
        authorizationInfo = new String(org.apache.commons.codec.binary.Base64.decodeBase64(authorizationInfo))

        val user_pwd = authorizationInfo.split(":")
        if (user_pwd.length != 2) {
            return BadRequest
        }
        if (!authenticate(user_pwd(0), user_pwd(1))) {
            return Unauthorized(
                "Authorization failed.",
                "Basic realm=\""+authenticationRealm+"\"")
        }

        super.processRequest(requestBody)
    }

}
