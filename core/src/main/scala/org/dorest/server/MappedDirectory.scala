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

import io.Source
import java.lang.Boolean

/**
 * Makes the content of a directory accessible.
 *
 * @author Michael Eichberg (mail at michael-eichberg.de)
 */
class MappedDirectory(val baseDirectory: String, enableIndexHTMLDeliveryOnDirectoryAccess: Boolean = false) extends Handler {

    import java.io._

    var path: String = _

    def processRequest(requestBody: => InputStream): Response = {
        if (method != GET) {
            return new SupportedMethodsResponse(GET)
        }

        var file = new File(baseDirectory + "/" + path)
        if (!file.exists) {
            return NotFoundResponse
        }

        if (file.isDirectory) {
            if (!enableIndexHTMLDeliveryOnDirectoryAccess) {
                return new Forbidden("Browsing directories is forbidden.")
            } else {
                file = new File(baseDirectory + "/" + path + "/index.html")
                if (!file.exists) {
                    return NotFoundResponse
                }
            }
        }

        val fileName = file.getName
        val fileType = {
            val fileSuffix = fileName.substring(fileName.lastIndexOf('.') + 1)
            Some((
                fileSuffix match {
                    case "css"        ⇒ MediaType.TEXT_CSS
                    case "javascript" ⇒ MediaType.APPLICATION_JAVASCRIPT
                    case "js"         ⇒ MediaType.APPLICATION_JAVASCRIPT
                    case "htm"        ⇒ MediaType.TEXT_HTML
                    case "html"       ⇒ MediaType.TEXT_HTML
                    case "xml"        ⇒ MediaType.APPLICATION_XML
                    case "txt"        ⇒ MediaType.TEXT_PLAIN
                    case "jpg"        ⇒ MediaType.IMAGE_JPEG
                    case "pdf"        ⇒ MediaType.APPLICATION_PDF
                    case "png"        ⇒ MediaType.IMAGE_PNG
                    case "ico"        ⇒ MediaType.IMAGE_X_ICON
                    case "svg"        ⇒ MediaType.IMAGE_SVG_XML
                    case _            ⇒ throw new Error("Media type detection based on file suffix (" + fileSuffix + ") failed: " + fileName)
                },
                // We are not able to reliably determine the used charset..
                None
            ))
        }

        // TODO Check that the accept header supports the file's media type.

        new OkResponse {

            val headers = new DefaultResponseHeaders

            val body = Some(new ResponseBody {

                def contentType = fileType

                def length = file.length.asInstanceOf[Int]

                def write(responseBody: OutputStream) {
                    // TODO Read blocks and not just single bytes.
                    // TODO Think about caching files.
                    /*val in = new FileInputStream(file)
                    try {
                        while (in.available > 0)
                            responseBody.write(in.read)
                    } finally {
                        if (in != null)
                            in.close
                    }*/
                    responseBody.write(readFully(file))
                }

                def readFully(file: File) : Array[Byte] = {
                    val in = new FileInputStream(file)
                    try {
                        val length = file.length.asInstanceOf[Int];
                        val data = new Array[Byte](length);
                        var read = 0;
                        while (read < length) {
                            read += in.read(data, read, length - read);
                        }
                        return data;
                    } finally {
                        if (in != null)
                            in.close();
                    }
                }
            })
        }
    }
}

