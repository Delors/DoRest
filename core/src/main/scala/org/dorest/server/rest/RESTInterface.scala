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
package rest

import java.io._


/**
 * Main trait of all Resources.
 *
 * @author Michael Eichberg
 */
trait RESTInterface extends Handler {

    private val getHandlers = scala.collection.mutable.ListBuffer[RepresentationFactory[_]]()
    private val postHandlers = scala.collection.mutable.ListBuffer[PostHandler]()
    private val putHandlers = scala.collection.mutable.ListBuffer[PutHandler]()
    private var deleteHandler: Option[() => Boolean] = None

    /**
     * The response code (sometimes also called status code) send to the client.
     */
    protected var responseCode = 200 // OK

    /**
     * This response's http headers.
     *
     * The Content-type and Content-length headers are automatically set based on the response body.
     */
    protected val responseHeaders = new DefaultResponseHeaders()

    /**
     * The content send to the client (if any).
     */
    protected var responseBody: Option[ResponseBody] = None

    /**
     * Analyzes the HTTP Request and dispatches to the correct (get,put,post,...) handler object.
     */
    def processRequest(requestBody: InputStream): Response = {
        method match {
            case GET if !getHandlers.isEmpty => {
                val mediaType = requestHeaders.getFirst("accept")
                if ((mediaType eq null) || (mediaType == "*/*")) {
                    responseBody = getHandlers.head.createRepresentation()
                    return Response(responseCode, responseHeaders, responseBody)
                }
                // TODO improve the search for a matching handler
                getHandlers.find(_.mediaType.toString == mediaType) match {
                    case Some(rbi) => {
                        responseBody = rbi.createRepresentation()
                        return Response(responseCode, responseHeaders, responseBody)
                    }
                    case None =>
                        return UnsupportedMediaTypeResponse
                }
            }
            case POST if !postHandlers.isEmpty => {
                // TODO nearly everything... matching...
                val postHandler = postHandlers.head
                postHandler.requestBodyHandler.process(None, requestBody)
                responseBody = postHandler.representationFactory.createRepresentation()
                return Response(responseCode, responseHeaders, responseBody)
            }
            case PUT if !putHandlers.isEmpty => {
                // TODO nearly everything... matching...
                val postHandler = putHandlers.head
                postHandler.requestBodyHandler.process(None, requestBody)
                responseBody = postHandler.representationFactory.createRepresentation()
                return Response(responseCode, responseHeaders, responseBody)
            }
            case DELETE if deleteHandler.isDefined => {
                if(!(deleteHandler.get)()){
                    return NotFoundResponse
                }

                return NoContent // delete was successful
            }
            case _ => {
                var supportedMethods: List[HTTPMethod] = Nil
                if (!getHandlers.isEmpty)
                    supportedMethods = GET :: supportedMethods;

                if (!putHandlers.isEmpty)
                    supportedMethods = PUT :: supportedMethods;

                if (!postHandlers.isEmpty)
                    supportedMethods = POST :: supportedMethods;

                if (!deleteHandler.isEmpty)
                    supportedMethods = DELETE :: supportedMethods;

                new SupportedMethodsResponse(supportedMethods)
            }
        }
    }


    /**
     *
     * '''Design''':
     * This object enables client to write the code in a more declarative meaning, e.g.:
     * {{{
     * get accepts …
     * }}}
     */
    final object get {
        def requests(t: RepresentationFactory[MediaType.Value]) {
            getHandlers += t
        }
    }

    abstract class RequestResponseHandlers(val requestBodyHandler: RequestBodyProcessor) {

        var representationFactory: RepresentationFactory[MediaType.Value] = _

        def returns(t: RepresentationFactory[MediaType.Value]) {
            representationFactory = t
            registerThisHandler
        }

        def registerThisHandler: Unit
    }


    final class PostHandler(requestBodyHandler: RequestBodyProcessor)
            extends RequestResponseHandlers(requestBodyHandler) {

        def registerThisHandler {
            postHandlers += this
        }

    }

    final object post {
        def receives(requestBodyHandler: RequestBodyProcessor) = new PostHandler(requestBodyHandler)
    }

    final class PutHandler(requestBodyHandler: RequestBodyProcessor)
            extends RequestResponseHandlers(requestBodyHandler) {

        def registerThisHandler {
            putHandlers += this
        }

    }

    final object put {
        def receives(requestBodyHandler: RequestBodyProcessor) = new PutHandler(requestBodyHandler)
    }

    /**
     * ===HTTP 1.1 Specification===
     * A successful response SHOULD be 200 (OK) if the response includes an entity describing the status, 202 (Accepted)
     * if the action has not yet been enacted, or 204 (No Content) if the action has been enacted but the response does
     * not include an entity.
     *
     * @return {{{true}}} if the specified resource was deleted.
     */
    final def delete(f: => Boolean) {
        deleteHandler = Some(() => f)
    }
}













