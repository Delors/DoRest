package org.dorest.server

import java.net._
import java.io._

/**
 * The core trait.
 */
trait Handler {

    /**
     * The used protocol. E.g., HTTP/1.1
     * <p>
     * This field will be set by the server before {@link #process()} is called.
     * </p>
     */
    var protocol: String = null

    var method: HTTPMethod.Value = null

    var requestURI: URI = null

    var remoteAddress: String = null

    var localAddress: String = null

    // We don't care about the specific data type used to store the request headers.
    // However, we need a way to extract specific headers.
    type HTTPHeaders = {
        def getFirst(key: String): String
    }

    var requestHeaders: HTTPHeaders = null

    /**
     * The core method that is responsible for handling the request.
     * <p>
     * The request body is a parameter of this method as it is subject
     * to various transformations.
     * </p>
     */
    def processRequest(requestBody: InputStream): Response

}