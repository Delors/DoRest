package de.smallnotes

import org.dorest.server.auth.{AuthenticatedUser, BasicAuthentication}

trait SmallNotesAuthorization
        extends BasicAuthentication
        with SmallNotesAuthenticator
        with AuthenticatedUser {

    def authenticationRealm = "iSmallNotes"
}




















