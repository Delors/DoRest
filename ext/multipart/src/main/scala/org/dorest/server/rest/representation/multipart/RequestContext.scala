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

package org.dorest.server.rest.representation.multipart

import org.apache.commons.fileupload.{ RequestContext => IRequestContext }
import org.apache.commons.io.IOUtils
import java.io._
import io.Codec
import java.nio.charset.Charset
import org.dorest.server.MediaType

/**
 * @author Mateusz Parzonka
 */
class RequestContext(charset: Option[Charset], in: InputStream, contentType: String, contentLength: Int) extends IRequestContext {

  def getCharacterEncoding: String = if (charset.isDefined) charset.get.toString else "US-ASCII"

  def getContentType: String = contentType

  def getContentLength: Int = contentLength

  def getInputStream(): InputStream = in

}