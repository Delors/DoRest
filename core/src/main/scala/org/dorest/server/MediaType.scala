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
 * List of all media types.
 */
object MediaType extends Enumeration {
    val any = Value("*/*")
    val APPLICATION_XML = Value("application/xml")
    val APPLICATION_JSON = Value("application/json")
    val TEXT_PLAIN = Value("text/plain")
    val TEXT_HTML = Value("text/html")
    val TEXT_CSS = Value("text/css")
    val APPLICATION_JAVASCRIPT = Value("application/javascript") // or "text/javascript"
    val IMAGE_GIF = Value("image/gif")
    val IMAGE_JPEG = Value("image/jpeg")
    val IMAGE_PNG = Value("image/png") // Portable Network Graphics; Registered,[7] Defined in RFC 2083
    val IMAGE_SVG_XML = Value("image/svg+xml") // SVG vector image; Defined in SVG Tiny 1.2 Specification Appendix M
    val IMAGE_TIFF = Value("image/tiff") //  Tag Image File Format (only for Baseline TIFF); Defined in RFC 3302
    val APPLICATION_ATOM_XML = Value("application/atom+xml") // Atom feeds
    val APPLICATION_PDF = Value("application/pdf")
    val IMAGE_X_ICON = Value("image/x-icon")
}