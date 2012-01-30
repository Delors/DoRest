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
package representation.multipart

import java.io._
import java.nio.charset.Charset
import scala.xml._
import org.apache.commons.fileupload._
import scala.collection.mutable.ListBuffer
import org.apache.commons.fileupload.disk.DiskFileItemFactory

/**
 * Implements support for requests containing multipart/form-data entities.
 *
 * @author Mateusz Parzonka
 */
trait MultipartSupport {
  
  /**
   * Files with a size less then 1MB are kept in RAM, bigger files are stored to the file repository.
   */
  private val SIZE_THRESHOLD = 1024*1024
  
  /**
   * Larger uploaded files are temporarly stored at this location.
   */
  private val FILE_REPOSITORY = new File("multipart-repository")

  def Multipart: RequestBodyProcessor = new RequestBodyProcessor(
    MediaType.MULTIPART_FORM_DATA,
    (charset: Option[Charset], in: InputStream) ⇒ {
      
      val fileRepository = new DiskFileItemFactory(SIZE_THRESHOLD, FILE_REPOSITORY)
      val fileUpload = new FileUpload(fileRepository)
      val iter = fileUpload.getItemIterator(new RequestContext(charset, in))
      val fileItemStreams = ListBuffer[FileItemStream]()
      while(iter.hasNext())
        fileItemStreams.+=(iter.next())
      _parts = fileItemStreams.toList
      
    })

  private var _parts: List[FileItemStream] = _

  def parts: List[FileItemStream] = _parts

}
