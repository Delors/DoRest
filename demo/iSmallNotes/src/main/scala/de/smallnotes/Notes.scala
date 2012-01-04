package de.smallnotes

import org.dorest.server.rest.RESTInterface
import org.dorest.server.rest.representation.orgjson.ORGJSONSupport
import org.json.{JSONObject, JSONArray}

class Notes
        extends RESTInterface
        with SmallNotesAuthorization
        with JDBCTransactionProvider
        with ORGJSONSupport {

    var t_id: Long = _

    get returns JSON {
        val stmt = connection.prepareStatement(
            """
            select n.id, n.note
            from "USERS" u,"TAGS" t,"NOTES_TAGS" nt,"NOTES" n
            where u.email =? and u.id = t.u_id and t.id=? and t.id=nt.t_id and nt.n_id=n.id
            """
        )
        stmt.setString(1, authenticatedUser)
        stmt.setLong(2, t_id)
        val rs = stmt.executeQuery()
        val ja = new JSONArray()
        while (rs.next()) {
            val jo = new JSONObject()
            jo.put("id", rs.getInt("id"))
            jo.put("note", rs.getString("note"))
            ja.put(jo)
        }
        ja
    }

    post of JSON returns JSON {
        val note: String = JSONRequestBody.get("note").toString
        val call = connection.prepareCall("{call add_note(?,?,?,?)}")
        call.registerOutParameter(4, java.sql.Types.BIGINT)
        call.setString(1, authenticatedUser)
        call.setLong(2, t_id)
        call.setString(3, note)
        call.execute()
        val jo = new JSONObject()
        jo.put("id", call.getLong(4))
        jo.put("note", note)
        jo
    }

}




















