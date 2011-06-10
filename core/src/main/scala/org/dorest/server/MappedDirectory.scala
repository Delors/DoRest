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
 * Makes the content of a directory accessible.
 */
class MappedDirectory(val baseDirectory: String) extends Handler {

    import java.io._

    var path: String = _

    def processRequest(requestBody: InputStream): Response = {
        if (method != GET) {
            return new SupportedMethodsResponse(GET)
        }

        val file = new File(baseDirectory+"/"+path)
        if (!file.exists) {
            return NotFoundResponse
        }

        if (file.isDirectory) {
            return new Forbidden("Browsing directories is forbidden.")
        }

        new OkResponse {
            // we ignore the accept header for now
            // TODO encoding... etc.
            val headers = new DefaultResponseHeaders
            val body = new ResponseBody {

                lazy val contentType = {
                    val fileName = file.getName()
                    Some((fileName.substring(fileName.lastIndexOf('.')) match {
                        case "css"  => MediaType.CSS
                        case "js"   => MediaType.JAVASCRIPT
                        case "html" => MediaType.HTML
                        case "xml"  => MediaType.XML
                        case "txt"  => MediaType.TEXT
                        case _      => throw new Error("Media type detection based on file suffix failed: "+fileName)
                    },
                        // We are not able to determine the used charset..
                        None))
                }

                def length = file.length.asInstanceOf[Int]

                def write(responseBody: OutputStream) {

                    // TODO use apache.commons.io...
                    val in = new FileInputStream(file)
                    try {
                        while (in.available > 0)
                            responseBody.write(in.read)
                    } finally {
                        if (in != null)
                            in.close
                    }
                }
            }
        }
    }
}