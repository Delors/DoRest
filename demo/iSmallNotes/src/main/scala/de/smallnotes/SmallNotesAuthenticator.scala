package de.smallnotes

import org.dorest.server.auth.Authentication

trait SmallNotesAuthenticator
  extends Authentication
  with JDBCConnectionProvider {

  def authenticationRealm = "iSmallNotes"

  def password(username: String): Option[String] = {
    val stmt = connection.prepareStatement("select pwd from \"public\".\"USERS\" where email = ?")
    stmt.setString(1, username)
    val rs = stmt.executeQuery()
    if (rs.next())
      Option(rs.getString("pwd"))
    else
      None

  }

  def authenticate(user: String, pwd: String): Boolean = {
    val stmt = connection.prepareStatement("select pwd from \"public\".\"USERS\" where email = ?")
    stmt.setString(1, user)
    val rs = stmt.executeQuery()
    rs.next() && rs.getString("pwd") == pwd
  }
}




















