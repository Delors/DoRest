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
package representation
package orgjson

import org.json._
import java.io._
import java.nio.charset._
import io.Codec
import io.Source

/**
 * Adds support for parsing and generating JSON representations using the <a href="http://www.json.org/java/">org.json
 * </a> library.
 *
 * '''Remark:''' As of Scala 2.9.0 the JSON library included with Scala is not really maintained and only handles a
 * subset of full JSON.
 */
trait ORGJSONSupport {

    /**
     * Generates a JSON representation for the JSONObject.
     */
    def JSON(getJSONObject: => JSONObject) =
        RepresentationFactory(MediaType.JSON) {
            Some(new UTF8BasedRepresentation(MediaType.JSON, Codec.toUTF8(getJSONObject.toString)))
        }

    private[this] var body: JSONObject = _

    def JSON: (MediaType.Value, (Option[Charset], InputStream) => Unit) = (
            MediaType.JSON,
            (charset: Option[Charset], in: InputStream) => {
                charset match {
                    case Some(charset) =>

                        //body = new JSONObject(org.apache.commons.io.IOUtils.toString(in, charset.displayName))
                        body = new JSONObject(Source.fromInputStream(in)(Codec(charset)).mkString)
                    case _ =>
                        // TODO Do we want to have this case? If yes, should we use ISO LATIN 1 (need to look at the HTTP spec.)
                        body = new JSONObject(org.apache.commons.io.IOUtils.toString(in))
                }
            }
            )

    def JSONRequestBody: JSONObject =
        if (body == null)
            throw new Error("The request body's media type is not application/json.")
        else
            body

}