package org.dorest.server
package auth

/**
 * @param text The information (text/plain; charset=UTF-8) send to the user; e.g., how to log in.<br>
 * <i>If you want to send, e.g., an HTML page this class cannot be used.</i>
 * @param www_authenticate the value of the "WWW-Authenticate" header. The precise value depends
 * on the chosen mechanism (e.g., Digest or Basic)
 */
class Unauthorized(
    text: String,
    www_authenticate: String)
        extends TextResponse(401, text) {

    headers.set("WWW-Authenticate", www_authenticate)
}
