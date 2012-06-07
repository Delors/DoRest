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
 * '''From the HTTP Spec.''':
 * <blockquote>
 *     Note: HTTP/1.1 servers are allowed to return responses which are
 *     not acceptable according to the accept headers sent in the
 *     request. In some cases, this may even be preferable to sending a
 *     406 response. User agents are encouraged to inspect the headers of
 *     an incoming response to determine if it is acceptable.
 * </blockquote>
 *
 * @author Michael Eichberg
 */
object NotAcceptableResponse extends PlainResponse(406)































