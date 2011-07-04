package de.smallnotes

import java.sql.{Connection, DriverManager}
import java.io.InputStream
import org.dorest.server.{Response, Handler}

trait JDBCTransactionProvider
        extends Handler
        with JDBCConnectionProvider {

    private[this] var _connection: Connection = _ // to enable the delayed init once..

    def connection: Connection = _connection

    override abstract def processRequest(requestBody: InputStream): Response = {
        // TODO implement connection pooling!
        _connection = DriverManager.getConnection("jdbc:postgresql://localhost/SmallNotes", "SmallNotesAdmin", "startSmallNotes");
        _connection.setAutoCommit(false)

        try {
            val response = super.processRequest(requestBody)
            _connection.commit()
            response
        } catch {
            case ex => {
                _connection.rollback()
                throw ex
            }
        }
        finally {
            _connection.close()
        }
    }
}




















