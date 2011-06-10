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