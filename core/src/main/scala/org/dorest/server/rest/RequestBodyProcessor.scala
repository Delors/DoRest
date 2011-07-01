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
import java.nio.charset._

/**
 * A RequestBodyProcessor is responsible for transforming a request body's
 * data – which is provided by an {{{InputStream}}} – into a suitable object-
 * oriented representation.
 *
 * E.g., given the request body's input stream and the used charset (if
 * specified and relevant), the body is then read and made available to the
 * resource handler in a meaningful way.
 *
 * @author Michael Eichberg
 */
class RequestBodyProcessor(
    val mediaType: MediaType.Value,
    val process: (Option[Charset], InputStream) ⇒ Unit)