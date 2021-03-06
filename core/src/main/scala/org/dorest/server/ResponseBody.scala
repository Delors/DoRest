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
import java.io.OutputStream

/**
  * Encapsulates a response's body.
  *
  * @author Michael Eichberg
  */
trait ResponseBody {

    /**
      * The body's content type (and charset).
      */
    def contentType: Option[(MediaType.Value, Option[Charset])]

    /**
      * The number of bytes that will be send back.
      */
    def length: Int

    /**
      * Writes out the body of the response.
      *
      * '''Control Flow''': Called by the DoRest framework – after sending the HTTP header – to write
      * out the response's body.
      *
      * '''Contract''': Exactly as many bytes have to be written as returned by length.
      */
    def write(responseBody: OutputStream): Unit
}





























