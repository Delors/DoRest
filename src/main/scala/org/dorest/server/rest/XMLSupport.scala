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

import java.io._

trait XMLSupport {

    import Utils._

    def XML(makeXML: => scala.xml.Node) =
        RepresentationFactory(MediaType.XML) {
            new Representation[MediaType.XML.type] {

                val response = toUTF8(makeXML.buildString(false))

                def contentType = Some((MediaType.XML, Some(UTF8)))

                def length = response.length

                def write(out: OutputStream) {
                    out.write(response)
                }

            }
        }
}