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
 * Implements common functionality required when embedding DoRest.
 * 
 * '''Remark''' This class is generally only relevant for developers who want to extend/embed DoRest.
 * 
 * @author Michael Eichberg
 */
trait DoRestServer {

    // TODO Do we also have to set the Content-length header (in the general case?)
    protected final def setContentTypeResponseHeader(responseHeaders: ResponseHeaders, responseBody: ResponseBody) {
        responseBody.contentType match {
            case Some((mediaType, None)) => {
                val contentType = mediaType.toString
                responseHeaders.set("Content-Type", contentType)
            }
            case Some((mediaType, Some(charset))) => {
                val contentType = mediaType.toString + "; charset=" + charset.displayName
                responseHeaders.set("Content-Type", contentType)
            }
            case _ => /*OK*/
        }
    }

}




