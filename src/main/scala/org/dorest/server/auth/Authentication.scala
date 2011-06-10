package org.dorest.server.auth

trait Authentication {
    /**
     * Tries to authenticate the given user. If the authentication
     * fails, <code>false</code> has to be returned.<p>
     * If the user credentials, in particular the user name,
     * is later on required, it is possible to additionally use, e.g.,
     * the trait {@link AuthenticatedUser}.
     */
    def authenticate(user: String, pwd: String): Boolean
}
