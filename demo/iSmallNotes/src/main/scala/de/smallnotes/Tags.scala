package de.smallnotes

import org.dorest.server.rest.RESTInterface
import org.dorest.server.rest.representation.orgjson.ORGJSONSupport
import org.json.{JSONObject, JSONArray}

class Tags
        extends RESTInterface
        with SmallNotesAuthorization
        with JDBCTransactionProvider
        with ORGJSONSupport {


    get returns JSON {
        val stmt = connection.prepareStatement(
            """ select t.id, t.tag
                from "USERS" u, "TAGS" t
                where u.email =? and u.id=t.u_id
            """)
        stmt.setString(1, authenticatedUser.get)
        val rs = stmt.executeQuery()
        val ja = new JSONArray()
        while (rs.next()) {
            val jo = new JSONObject()
            jo.put("tag", rs.getString("tag"))
            jo.put("id", rs.getInt("id"))
            ja.put(jo)
        }
        ja
    }


    post of JSON returns JSON {
        val tag: String = JSONRequestBody.get("tag").toString
        // TODO implement as callable statement

        val insertStmt = connection.prepareStatement(
            """ insert into "TAGS"(tag,u_id)
                select ?, id from "USERS" where email = ?
            """)
        insertStmt.setString(1, tag)
        insertStmt.setString(2, authenticatedUser.get)
        insertStmt.executeUpdate()

        // return the newly created resource...
        val selectStmt = connection.prepareStatement(
            """
            select t.id from "TAGS" t, "USERS" u
            where t.tag = ? and t.u_id = u.id and u.email = ?
            """
        )
        selectStmt.setString(1, tag)
        selectStmt.setString(2, authenticatedUser.get)
        val rs = selectStmt.executeQuery()
        if (rs.next()) {
            val jo = new JSONObject()
            jo.put("tag", tag)
            jo.put("id", rs.getInt("id"))
            Some(jo)
        } else {
            None
        }
    }
}




















