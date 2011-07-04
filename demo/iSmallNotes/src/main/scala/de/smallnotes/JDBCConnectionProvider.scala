package de.smallnotes

import java.sql.Connection

trait JDBCConnectionProvider {

    def connection: Connection

}




















