package org.dorest.server.rest.representation.multipart

import org.apache.commons.fileupload.{RequestContext => IRequestContext}
import org.apache.commons.io.IOUtils
import java.io._
import io.Codec
import java.nio.charset.Charset
import org.dorest.server.MediaType

/**
 * 
 * @author Mateusz Parzonka
 * 
 */
class RequestContext(charset: Option[Charset], in: InputStream) extends IRequestContext {
  
    def getCharacterEncoding: String = if (charset.isDefined) charset.get.toString else "TODO"
      
    def getContentType: String = MediaType.MULTIPART_FORM_DATA.toString
    
    // TODO if we could access the content-header, then we would have the length there.
    def getContentLength: Int = IOUtils.toByteArray(in).length
    
    def getInputStream(): InputStream = in


}