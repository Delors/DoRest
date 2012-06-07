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
 * Handlers are responsible for processing requests.
 *
 * The core method is the [[#processRequest(InputStream):Response]] method. It will be called by the server component
 * after the request's path and query was successfully matched.
 *
 * If a handler fails to process the request, it is the responsibility
 * of the handler to create a [[org.dorest.server.Response]] object that appropriately describes the error. A server
 * adapter must not try to guess the error condition. If the process' request method fails with an exception, the server
 * is expected to return a response that just states that an internal server error (response code 500) was encountered.
 *
 * @author Michael Eichberg
 */
trait Handler {

    /**
     * The used protocol. E.g., HTTP/0.9, HTTP/1.0 or HTTP/1.1
     *
     * '''Control Flow''':
     * This field will be set by the server before [[#processRequest(InputStream):Response]] is called.
     *
     * '''Remark''':
     * This field is only to be written by the server component.
     */
    var protocol: String = _

    /**
     * The HTTP method of this request.
     *
     * '''Control Flow''':
     * This field will be set by the server before [[#processRequest(InputStream):Response]] is called.
     *
     * '''Remark''':
     * This field is only to be written by the server component.
     */
    var method: HTTPMethod = _

    /**
     * The (complete) URI of the request.
     *
     * The URI is primarily provided for debugging and logging purposes. The relevant parts of an URI are
     * expected to be extracted by path matchers (cf. [[org.dorest.server.HandlerFactory]]).
     *
     * '''Control Flow''':
     * This field will be set by the server before [[#processRequest(InputStream):Response]] is called.
     *
     * '''Remark''':
     * This field is only to be written by the server component.
     */
    var requestURI: URI = _

    /**
     * The address of the client.
     *
     * '''Control Flow''':
     * This field will be set by the server before [[#processRequest(InputStream):Response]] is called.
     *
     * '''Remark''':
     * This field is only to be written by the server component.
     */
    // TODO should we replace this field by "remoteHostName" and "remotePort"?
    var remoteAddress: String = _

    /**
     * The local address.
     *
     * '''Control Flow''':
     * This field will be set by the server before [[#processRequest(InputStream):Response]] is called.
     *
     * '''Remark''':
     * This field is only to be written by the server component.
     */
    // TODO should we replace this field by "loaclHostName" and "localPort"?
    var localAddress: String = _

    /**
     * The precise data type used to store the request headers is not relevant, but it has to be possible
     * to extract specific headers.
     */
    type HTTPHeaders = {
        def getFirst(key: String): String
    }

    /**
     * The request's headers.
     *
     * '''Control Flow''':
     * This field will be set by the server before [[#processRequest(InputStream):Response]] is called.
     *
     * '''Remark''':
     * This field is only to be written by the server component.
     */
    var requestHeaders: HTTPHeaders = _

    /**
     * Processes a request.
     *
     * '''Overriding This Method''':
     * When your handler overrides this method and reads from the stream, it is the responsibility of
     * your handler to pass on a new InputStream to upstream handlers which the latter ones can
     * use to read the complete (e.g. decrypted, unpacked) request body.
     *
     * '''Design''':
     * The request body is a parameter of this method as it is subject
     * to various transformations and it may not be possible to read it multiple times.
     */
    // TODO Is it Ok if we ignore the request body (take a look in the specification and define required behavior of the server adapters).
    def processRequest(requestBody: => InputStream): Response

}