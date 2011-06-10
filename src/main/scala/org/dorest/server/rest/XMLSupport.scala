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