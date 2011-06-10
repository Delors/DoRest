package org.dorest.server

object MediaType extends Enumeration {
    val any = Value("*/*")
    val XML = Value("application/xml")
    val JSON = Value("application/json")
    val TEXT = Value("text/plain")
    val HTML = Value("text/html")
    val CSS = Value("text/css")
    val JAVASCRIPT = Value("application/javascript") // or "text/javascript"
    val GIF = Value("image/gif")
    val JPEG = Value("image/jpeg")
    val PNG = Value("image/png") // Portable Network Graphics; Registered,[7] Defined in RFC 2083
    val SVG = Value("image/svg+xml") // SVG vector image; Defined in SVG Tiny 1.2 Specification Appendix M
    val TIFF = Value("image/tiff") //  Tag Image File Format (only for Baseline TIFF); Defined in RFC 3302
    val ATOM = Value("application/atom+xml") // Atom feeds
    val PDF = Value("application/pdf")
}