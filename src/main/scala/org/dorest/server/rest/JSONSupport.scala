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

import org.json._
import java.io._
import java.nio.charset._

trait JSONSupport {

    import Utils._

    def JSON(getJSONObject: => Object) =
        RepresentationFactory(MediaType.JSON) {
            new UTF8BasedRepresentation(MediaType.JSON, toUTF8(getJSONObject.toString))
        }

    private[this] var body: JSONObject = _

    def JSON: (MediaType.Value, (Option[Charset], InputStream) => Unit) =
        (MediaType.JSON,
            (charset: Option[Charset], in: InputStream) => {
                charset match {
                    case Some(charset) =>
                        body = new JSONObject(org.apache.commons.io.IOUtils.toString(in, charset.displayName))
                    case _ =>
                        // TODO Do we want to have this case? If yes, should we use ISO LATIN 1 (need to look at the HTTP spec.)
                        body = new JSONObject(org.apache.commons.io.IOUtils.toString(in))
                }
            })

    def JSONRequestBody: JSONObject =
        if (body == null)
            throw new Error("The body's media type does not match the spcified type.")
        else
            body

}