/*
   Copyright 2012 Michael Eichberg et al

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

/** Utility function to facilitate the matching of URIs.
  *
  * @author Michael Eichberg
  */
trait URIsMatcher {

    type PathMatcher = ( /*path*/ String) ⇒ Option[( /*query*/ String) ⇒ Option[Handler]]

    // NEEDS TO BE PROVIDED BY THE CLASS WHERE THIS CLASS IS MIXED IN
    def register(handlerFactory: HandlerFactory): Unit

    def addPathMatcher(pathMatcher: PathMatcher) {
        register(new HandlerFactory {
            def matchURI(path: String, query: String): Option[Handler] = {
                pathMatcher(path) match {
                    case Some(qm) ⇒ qm(query)
                    case None     ⇒ None
                }
            }
        })
    }

    /** Registers a partial function that tries to match a request's path and query string.
      *
      * '''Usage Scenario'''
      * {{{
      * val PathExtractor = """/static/(.*)""".r
      * val SuffixExtractor = """.*suffix=(.+)""".r
      * addMatcher({
      * 	case (PathExtractor(path),SuffixExtractor(suffix)) => new Handler{...}
      * })
      * }}}
      *
      * @param matcher A partial function that takes a tuple of strings, where the first string is the
      * current path and the second string is the query part. The return value is (if both are matched) a
      * valid (non-null) handler object.
      */
    def addMatcher(matcher: PartialFunction[( /*path*/ String, /*query*/ String), Handler]) {
        register(new HandlerFactory {
            def matchURI(path: String, query: String): Option[Handler] = {
                if (matcher.isDefinedAt((path, query)))
                    Some(matcher((path, query)))
                else
                    None
            }
        })
    }

    /** Use ROOT to match a URI that ends with "/" and where all previous segments have been successfully
      * matched.
      */
    object ROOT {
        def unapply(pathSegment: String): Boolean = {
            !(pathSegment eq null) && pathSegment.length == 0
        }
    }

    /** Use MATCHED() to match a URI that does not end with "/".
      */
    object MATCHED {
        def unapply(pathSegment: String): Boolean = {
            pathSegment eq null
        }
    }

    /** Use STRING(s) to match a path segment that is non-empty.
      */
    object STRING {
        def unapply(pathSegment: String): Option[String] = {
            if (!(pathSegment eq null) && pathSegment.length > 0)
                Some(pathSegment)
            else
                None
        }
    }

    object LONG {
        def unapply(pathSegment: String): Option[Long] = {
            if (pathSegment eq null) return None

            try {
                Some(java.lang.Long.parseLong(pathSegment, 10))
            }
            catch {
                case e: NumberFormatException ⇒ None
                case e                        ⇒ throw e;
            }
        }
    }

    object INT {
        def unapply(pathSegment: String): Option[Int] = {
            if (pathSegment eq null) return None

            try {
                Some(java.lang.Integer.parseInt(pathSegment, 10))
            }
            catch {
                case e: NumberFormatException ⇒ None
                case e                        ⇒ throw e;
            }
        }
    }

    case class /(matcher: PartialFunction[String, PathMatcher]) extends PathMatcher {

        /** @param completePath A valid URI path (or the yet unmatched part of the URI).
          * 		The completePath is either null or is a string that starts with a "/".
          * 		The semantics of null is that the complete path was matched; i.e., there
          * 		is no remaining part.
          */
        def apply(completePath: String): Option[(String) ⇒ Option[Handler]] = {
            if (completePath == null)
                return {
                    if (matcher.isDefinedAt(null))
                        matcher(null)(null)
                    else
                        // the provided path was completely matched, however it is too short
                        // w.r.t. a RESTful application; hence, this points to a design problem
                        None
                }

            if (completePath.charAt(0) != '/')
                throw new IllegalArgumentException("The provided path: \""+completePath+"\" is invalid; it must start with a /.")

            val path = completePath.substring(1) // we truncate the trailing "/"
            val separatorIndex = path.indexOf('/')
            val head = if (separatorIndex == -1) path else path.substring(0, separatorIndex)
            val tail = if (separatorIndex == -1 || (separatorIndex == 0 && path.length == 1)) null else path.substring(separatorIndex)
            if (matcher.isDefinedAt(head))
                matcher(head)(tail)
            else
                None

        }
    }

    implicit def HandlerToPathMatcher(h: Handler): PathMatcher = {
        (pathSegment: String) ⇒
            {
                if (pathSegment eq null)
                    Some((query: String) ⇒ /* the query is ignored */ Some(h))
                else
                    None
            }
    }

    object bind {
        def path(handlerInitializer: (String) ⇒ Handler): PathMatcher =
            (pathSegment: String) ⇒
                if (pathSegment eq null)
                    None
                else
                    Some((query: String) ⇒ Some(handlerInitializer(pathSegment)))
    }

}




