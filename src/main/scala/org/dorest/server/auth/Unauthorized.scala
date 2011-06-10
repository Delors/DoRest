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
package auth

/**
 * This response object can be used to send back the information that the request cannot be processed,
 * because the user is not authenticated. It is possible to provide further information to the user. In all cases the
 * mechanism how to authenticate needs to be specified.
 *
 * @param text The information (Content-Type: text/plain; charset=UTF-8) send to the user; e.g.,
 * describing that this site needs authentication and how to log in.<br>
 * <i>If you want to send other content than some plain text, e.g., an HTML page, this class cannot be used.</i>
 *
 * @param www_authenticate <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.47">the value of the
 * WWW-Authenticate header</href>. The precise value depends on the chosen mechanism (e.g., Digest or Basic)
 *
 * @author Michael Eichberg (mail at michael-eichberg.de)
 */
class Unauthorized(text: String, www_authenticate: String) extends TextResponse(401, text) {

    headers.set("WWW-Authenticate", www_authenticate)
}

object Unauthorized {


    def apply(text: String = "Authorization required.", www_authenticate: String) = {
        new Unauthorized(text, www_authenticate)
    }

}