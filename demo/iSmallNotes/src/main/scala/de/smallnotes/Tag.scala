package de.smallnotes

import org.dorest.server.rest.RESTInterface
import org.dorest.server.rest.representation.orgjson.ORGJSONSupport

class Tag
        extends RESTInterface
        with SmallNotesAuthorization
        with JDBCTransactionProvider
        with ORGJSONSupport {

    var id: Long = _

    delete {
        // TODO model as function... delete_user_tag
        val stmt = connection.prepareStatement("delete from \"TAGS\" where id=?")
        stmt.setLong(1, id)
        stmt.executeUpdate()
        true
    }
}




















