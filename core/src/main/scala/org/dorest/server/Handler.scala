/*
   Copyright 2011 Michael Eichberg et al

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
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