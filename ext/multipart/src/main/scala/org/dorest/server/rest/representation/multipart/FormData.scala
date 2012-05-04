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

import java.io.InputStream
import org.apache.commons.fileupload.FileItemStream
import org.dorest.server.MediaType
import java.nio.charset.Charset
import scala.io.Source
import org.apache.commons.io.IOUtils
import java.io.ByteArrayInputStream

/**
 * @author Mateusz Parzonka
 */
sealed case class FormData

case class FormField(fieldName: String) extends FormData {

  private[this] var _body: InputStream = _
  private[this] var _charset: Option[Charset] = _

  def this(fieldName: String, charset: Option[Charset], body: InputStream) = {
    this(fieldName)
    _body = body
    _charset = charset
  }

  def content: String = Source.fromInputStream(_body, _charset.getOrElse("US-ASCII").toString()).mkString("")

}

case class Data(val fieldName: String, val mediaType: MediaType.Value) extends FormData {

  private[this] var _body: Array[Byte] = _
  private[this] var _charset: Option[Charset] = _
  private[this] var _fileName: String = _

  def this(fieldName: String, mediaType: MediaType.Value, charset: Option[Charset], body: InputStream, fileName: String) = {
    this(fieldName, mediaType)
    _body = IOUtils.toByteArray(body)
    _charset = charset
    _fileName = fileName
  }

  def openStream: InputStream = new ByteArrayInputStream(_body)
  
  def contentLength = _body.size

  def charset = _charset
  
  def fileName = _fileName

}

object FormData {
  def apply(fis: FileItemStream) = {
    val contentType = MediaType.parseContentType(fis.getContentType())
    fis.isFormField() match {
      case true => new FormField(fis.getFieldName(), contentType.charset, fis.openStream())
      case false => new Data(fis.getFieldName(), contentType.mediaType, contentType.charset, fis.openStream(), fis.getName())
    }
  }
}
