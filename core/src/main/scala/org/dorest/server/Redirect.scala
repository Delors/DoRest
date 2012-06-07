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

import java.nio.charset.Charset
import java.io._

/**
 * Handler to send a "see-other" redirect (HTTP status code 303). The "Location" header sent back to the
 * client is set to the given location. The location information can be relative or absolute.
 *
 * This class is thread safe.
 *
 * @author Michael Eichberg
 */
class Redirect(val location: String) extends Handler {

    val response = new PlainResponse(303) { headers.set("Location", location) }

    def processRequest(requestBody: ⇒ InputStream) = {
        // we actually don't process the request at all
        response;
    }
}

abstract class DynamicRedirect extends Handler {

    def location: Option[String]

    private def response(): Response = {
        location match {
            case Some(l) ⇒ new PlainResponse(303) { headers.set("Location", l) }
            case None    ⇒ NotFoundResponse
        }
    }

    def processRequest(requestBody: ⇒ InputStream): Response = {
        // we actually don't process the request at all
        response();
    }
}












