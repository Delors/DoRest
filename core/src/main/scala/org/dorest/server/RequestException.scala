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
 * This exception can be caught by a DoRest server implementation returning the contained server response to the client.
 * Allows forwarding of responses from any point in the implementation regardless of return type of the containing method.
 *
 * @param response
 * 	typical usage: {{{ response = BadRequest("Content-Type not provided") }}}
 * @author Mateusz Parzonka
 */
case class RequestException(response: ErrorResponse) extends Exception
