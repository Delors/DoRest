package org.dorest.server
package rest

import java.io._

trait TEXTSupport {

    import Utils._

    def TEXT(getText: => String) =
        RepresentationFactory(MediaType.TEXT) {
            new UTF8BasedRepresentation(MediaType.TEXT, toUTF8(getText))
        }
}