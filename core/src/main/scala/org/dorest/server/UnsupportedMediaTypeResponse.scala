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

/**
 * Represents an `Unsupported Media Type Response`.
 *
 * From the specification:
 * <blockquote>
 * The server is refusing to service the request because the entity of the request is in a format not
 * supported by the requested resource for the requested method.
 * </blockquote>
 *
 * @see [[http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html RFC 2616 - Unsupported Media Type Response]]
 *
 * @author Michael Eichberg
 */
object UnsupportedMediaTypeResponse extends PlainResponse(415)


























