package org.dorest.server

object HTTPMethod extends Enumeration {
    val GET = Value("GET")
    val POST = Value("POST")
    val PUT = Value("PUT")
    val DELETE = Value("DELETE")

    // The following methods will be added, when needed...
    // val OPTIONS = Value("OPTIONS")
    // val HEAD = Value("HEAD")
    // val TRACE = Value("TRACE")
    // val CONNECT = Value("CONNECT")
}