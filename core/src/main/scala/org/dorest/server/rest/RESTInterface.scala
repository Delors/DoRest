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
import java.net.{ URL, URI }
import java.nio.charset.Charset

/**
 * Main trait of all Resources.
 *
 * @author Michael Eichberg
 * @author Mateusz Parzonka
 */
trait RESTInterface extends Handler {

  private val getHandlers = scala.collection.mutable.ListBuffer[RepresentationFactory[_]]()
  private val postHandlers = scala.collection.mutable.ListBuffer[PostHandler]()
  private val putHandlers = scala.collection.mutable.ListBuffer[PutHandler]()
  private var deleteHandler: Option[() => Boolean] = None

  /**
   * The response code (sometimes also called status code) send to the client.
   *
   * The default value is "200"; i.e., "OK".
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
   * Parses the content-type header, if specified and creates a ContentType.
   * Mapped exceptions are thrown when content-type is not provided or mediatype unknown.
   * A mediaType is unknown if not contained in MediaType.Values.
   */
  lazy val contentType: ContentType = {
    def charset(cs: String): Option[Charset] = if (Charset.isSupported(cs)) Some(Charset.forName(cs)) else None
    def mediaType(mt: String): MediaType.Value = if (MediaType.stringValues.contains(mt))
      MediaType.withName(mt)
    else
      throw new ResponseMappedException(ErrorResponse(401, "Unknown MediaType %s" format mt))
    requestHeaders.getFirst("Content-Type") match {
      case ct: String => ct.split("; charset=") match {
        case Array(mt, cs) => ContentType(mediaType(mt), charset(cs))
        case Array(mt) => ContentType(mediaType(mt), None)
        case _ => throw new ResponseMappedException(BadRequest("MediaType not provided"))
      }
      case _ => throw new ResponseMappedException(response = BadRequest("Content-Type not provided"))
    }
  }

  /**
   * Analyzes the HTTP Request and dispatches to the correct (get,put,post,delete) handler object.
   */
  def processRequest(requestBody: InputStream): Response = {

    method match {
      case GET if !getHandlers.isEmpty => {
        val mediaType = requestHeaders.getFirst("accept")
        if ((mediaType eq null) || (mediaType == "*/*")) {
          return Response(responseCode, responseHeaders, responseBody)
        }
        // TODO improve the search for a matching handler
        getHandlers.find(_.mediaType.toString == mediaType) match {
          case Some(rbi) => {
            responseBody = rbi.createRepresentation()
            responseBody match {
              // TODO reevaluate necessity of having an optional response-body
              // When a representation creates NONE this should encode a 404 -mateusz
              case Some(_: ResponseBody) => return Response(responseCode, responseHeaders, responseBody)
              case None => return NotFoundResponse
            }
          }
          case None => return NotAcceptableResponse
        }
      }
      case POST if !postHandlers.isEmpty => {
         // if we cannot handle the request body, we have to return a UnsupportedMediaTypeResponse (superfluous comment? -mateusz)
        // TODO nearly everything... matching... (done? -mateusz)
        postHandlers.find(_.requestBodyHandler.mediaType == contentType.mediaType) match {
          case Some(postHandler) => {
        	responseCode = 201 // TODO do we need mutable state? -mateusz
            postHandler.requestBodyHandler.process(contentType.charset, requestBody)
            responseBody = postHandler.representationFactory.createRepresentation()
            return Response(responseCode, responseHeaders, responseBody)
            }
          case None => return UnsupportedMediaTypeResponse
        }
      }
      case PUT if !putHandlers.isEmpty => {
        // TODO nearly everything... matching... (done? -mateusz)
        putHandlers.find(_.requestBodyHandler.mediaType == contentType.mediaType) match {
          case Some(putHandler) =>
            putHandler.requestBodyHandler.process(contentType.charset, requestBody)
            responseBody = putHandler.representationFactory.createRepresentation() 
            responseBody match {
              case Some(_: ResponseBody) => return Response(responseCode, responseHeaders, responseBody)
              case None => return NotFoundResponse
            }
          case None => return UnsupportedMediaTypeResponse
        }
      }
      case DELETE if deleteHandler.isDefined => {
        if (!(deleteHandler.get)()) {
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

        return new SupportedMethodsResponse(supportedMethods)
      }
    }

    NotFoundResponse
  }

  case class ContentType(mediaType: MediaType.Value, charset: Option[Charset])

  /**
   *
   * '''Design''':
   * This object enables client to write the code in a more declarative meaning, e.g.:
   * {{{
   * get accepts …
   * }}}
   */
  final object get {

    // just an alternative way to specify which media type is accepted
    def accepts(t: RepresentationFactory[MediaType.Value]) {
      returns(t)
    }

    def returns(t: RepresentationFactory[MediaType.Value]): RESTInterface = {
      getHandlers += t
      RESTInterface.this
    }
  }

  abstract class RequestResponseHandlers(val requestBodyHandler: RequestBodyProcessor) {

    var representationFactory: RepresentationFactory[MediaType.Value] = _

    def returns(t: RepresentationFactory[MediaType.Value]): RESTInterface = {
      representationFactory = t
      registerThisHandler
      RESTInterface.this
    }

    def registerThisHandler: Unit
  }

  final class PostHandler(requestBodyHandler: RequestBodyProcessor)
    extends RequestResponseHandlers(requestBodyHandler) {

    def registerThisHandler {
      postHandlers += this
    }

  }

  /**
   * ===HTTP 1.1 Specification===
   * If a resource has been created on the origin server, the response SHOULD
   * be 201 (Created) and contain an entity which describes the status of the
   * request and refers to the new resource, and a Location header.
   */
  final object post {

    /**
     * @see [[#of(RequestBodyProcessor)]]
     */
    def sends(requestBodyHandler: RequestBodyProcessor) = of(requestBodyHandler)

    def of(requestBodyHandler: RequestBodyProcessor): PostHandler = new PostHandler(requestBodyHandler)
  }

  final class PutHandler(requestBodyHandler: RequestBodyProcessor)
    extends RequestResponseHandlers(requestBodyHandler) {

    def registerThisHandler {
      putHandlers += this
    }

  }

  final object put {

    /**
     * @see [[#of(RequestBodyProcessor)]]
     */
    def sends(requestBodyHandler: RequestBodyProcessor) = of(requestBodyHandler)

    def of(requestBodyHandler: RequestBodyProcessor): PutHandler = new PutHandler(requestBodyHandler)
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

  /**
   * Sets a response-header's location field.
   *
   * ===HTTP 1.1 Specification===
   * The Location response-header field is used to redirect the recipient to
   * a location other than the Request-URI for completion of the request or
   * identification of a new resource. For 201 (Created) responses, the
   * Location is that of the new resource which was created by the request.
   *
   * For 3xx responses, the location SHOULD indicate the server's preferred
   * URI for automatic redirection to the resource. The field value consists
   * of a single absolute URI.
   *
   *  Location       = "Location" ":" absoluteURI
   *
   */
  final def Location(location: URL) {
    responseHeaders.set("Location", location.toString) // TODO figure out which is the correct encoding
  }

  /**
   * Sets a response-header's location field.
   *
   * ===HTTP 1.1 Specification===
   * The Location response-header field is used to redirect the recipient to
   * a location other than the Request-URI for completion of the request or
   * identification of a new resource. For 201 (Created) responses, the
   * Location is that of the new resource which was created by the request.
   *
   * For 3xx responses, the location SHOULD indicate the server's preferred
   * URI for automatic redirection to the resource. The field value consists
   * of a single absolute URI.
   *
   *  Location       = "Location" ":" absoluteURI
   *
   * /
   * final def relativeLocation(path: String ){
   *
   * println(requestURI)
   * val baseURL = new URL("http",localAddress,requestURI.toString)
   * val location = new URL(baseURL,path)
   * responseHeaders.set("Location",location.toString) // TODO figure out which is the correct encoding
   * }
   */
}
