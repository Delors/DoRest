package org.dorest.server
package auth

import java.io.InputStream

/**
 * Implements  <a href="http://www.ietf.org/rfc/rfc1945.txt.pdf">HTTP basic access authentication</a>).
 *
 * @see SimpleAuthenticator
 */
trait BasicAuthentication extends Authentication with Handler {

    /**
     * The required authentication realm, e.g. "My Website", that is sent back if
     * the request does not contain the necessary authorization credentials.
     */
    def authenticationRealm : String

    override abstract def processRequest(requestBody : InputStream) : Response = {
        var authorizationInfo = requestHeaders.getFirst("Authorization")
        if (authorizationInfo == null) {
            return new Unauthorized(
                "Authorization required",
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
            return new Unauthorized(
                "You are not authorized.",
                "Basic realm=\""+authenticationRealm+"\"")
        }

        super.processRequest(requestBody)
    }

}
