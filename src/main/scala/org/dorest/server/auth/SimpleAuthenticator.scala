package org.dorest.server
package auth

trait SimpleAuthenticator extends Authentication {
    
    val authorizationUser: String
    val authorizationPwd: String

    def authenticate(user: String, pwd: String): Boolean = {
        (user == authorizationUser) && (pwd == authorizationPwd)
    }
}