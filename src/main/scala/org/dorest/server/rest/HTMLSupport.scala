package org.dorest.server
package rest

import org.json._
import java.net._
import java.io._
import java.nio.charset._

trait HTMLSupport {

    import Utils._

    def HTML(getHTML: => String) =
        RepresentationFactory(MediaType.HTML) {
            new UTF8BasedRepresentation(MediaType.HTML, toUTF8(getHTML))
        }
}
