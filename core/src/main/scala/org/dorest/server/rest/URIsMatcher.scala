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

/**
  * Provides support for matching URIs.
  *
  * @author Michael Eichberg
  */
trait URIsMatcher { this: DoRestApp ⇒

    /**
      * Register a partial function that tries to match a request's path and query string.
      *
      * @example
      * {{{
      * val PathExtractor = """/static/(.*)""".r
      * val SuffixExtractor = """.*suffix=(.+)""".r
      * addMatcher({
      * 	case (PathExtractor(path),SuffixExtractor(suffix)) => new Handler{...}
      * })
      * }}}      *
      * @param matcher A partial function that takes a tuple of strings, where the first string will be the
      * current path and the second string will be the query part. The return value has to be – if both are
      * matched – a valid (non-null) handler object.
      */
    def addMatcher(matcher: PartialFunction[( /*path*/ String, /*query*/ String), Handler]) {
        register(new HandlerFactory {
            def matchURI(path: String, query: String): Option[Handler] = matcher.lift((path, query))
        })
    }

    type URIMatcher = ( /*path*/ String) ⇒ Option[( /*query*/ String) ⇒ Option[Handler]]

    def addURIMatcher(uriMatcher: URIMatcher) {
        register(new HandlerFactory {
            def matchURI(path: String, query: String): Option[Handler] = {
                uriMatcher(path) match {
                    case Some(qm) ⇒ qm(query)
                    case None     ⇒ None
                }
            }
        })
    }

    //
    //
    // CODE RELATED TO MATCHING THE PATH OF AN URI
    //
    //

    /**
      * Use ROOT to match (the remaining) path of an URI that ends with "/" and where all previous segments have been
      * successfully matched.
      */
    object ROOT {
        def unapply(pathSegment: String): Boolean = {
            !(pathSegment eq null) && pathSegment.length == 0
        }
    }

    /**
      * Use MATCHED() to match a URI that does not end with "/".
      */
    object MATCHED {
        def unapply(pathSegment: String): Boolean = {
            pathSegment eq null
        }
    }

    case class /(matcher: PartialFunction[String, URIMatcher]) extends URIMatcher {

        /**
          * @param trailingPath A valid URI path (or the yet unmatched part of the path of an URI).
          * 		The trailingPath is either null or is a string that starts with a "/".
          * 		The semantics of null is that the complete path was matched; i.e., there
          * 		is no remaining part.
          */
        def apply(trailingPath: String): Option[(String) ⇒ Option[Handler]] = {
            if (trailingPath == null)
                return {
                    if (matcher.isDefinedAt(null))
                        matcher(null)(null)
                    else
                        // the provided path was completely matched, however it is too short
                        // w.r.t. a RESTful application; hence, this points to a design problem
                        None
                }

            if (trailingPath.charAt(0) != '/')
                throw new IllegalArgumentException("The provided path: \""+trailingPath+"\" is invalid; it must start with a /.")

            val path = trailingPath.substring(1) // we truncate the trailing "/"
            val separatorIndex = path.indexOf('/')
            val head = if (separatorIndex == -1) path else path.substring(0, separatorIndex)
            val tail = if (separatorIndex == -1 || (separatorIndex == 0 && path.length == 1)) null else path.substring(separatorIndex)
            if (matcher.isDefinedAt(head))
                matcher(head)(tail)
            else
                None
        }
    }

    implicit def HandlerToPathMatcher(h: Handler): URIMatcher = {
        (pathSegment: String) ⇒
            {
                if (pathSegment eq null)
                    Some((query: String) ⇒ /* the query is ignored */ Some(h))
                else
                    None
            }
    }

    //
    //
    // CODE RELATED TO MATCHING THE QUERY PART OF URIs
    //
    //

    case class ?(matcher: PartialFunction[URIQuery, Handler]) extends URIMatcher {
        def apply(path: String): Option[String ⇒ Option[Handler]] = {
            if (path ne null)
                None
            else {
                Some((query: String) ⇒ {
                    val splitUpQuery = org.dorest.server.utils.URIUtils.decodeRawURLQueryString(query)
                    if (matcher.isDefinedAt(splitUpQuery)) {
                        Some(matcher(splitUpQuery))
                    }
                    else {
                        None
                    }
                })
            }
        }
    }

    type URIQuery = Map[String /*query key*/ , Seq[String] /* values associated with the key*/ ]

    trait QueryMatcher {
        protected def apply[T](kvMatcher: KeyValueMatcher[T], uriQuery: URIQuery): Option[T] = {
            val (key, valueMatcher) = kvMatcher
            uriQuery.get(key) match {
                case Some(values) ⇒ {
                    if (valueMatcher.isDefinedAt(values))
                        Some(valueMatcher(values))
                    else
                        None
                }
                case None ⇒ None
            }
        }
    }
    type KeyValueMatcher[T] = (String, PartialFunction[Seq[String], T])

    class QueryMatcher1[T1](val kvMatcher1: KeyValueMatcher[T1]) extends QueryMatcher {
        def unapply(uriQuery: URIQuery): Some[Option[T1]] = {
            Some(apply(kvMatcher1, uriQuery))
        }
    }

    class QueryMatcher2[T1, T2](val kvm1: KeyValueMatcher[T1], val kvm2: KeyValueMatcher[T2]) extends QueryMatcher {
        def unapply(uriQuery: URIQuery): Some[(Option[T1], Option[T2])] = {
            Some((apply(kvm1, uriQuery), apply(kvm2, uriQuery)))
        }
    }

    object QueryMatcher {
        def apply[T1](kvm1: KeyValueMatcher[T1]) =
            new QueryMatcher1(kvm1)

        def apply[T1, T2](kvm1: KeyValueMatcher[T1], kvm2: KeyValueMatcher[T2]) =
            new QueryMatcher2(kvm1, kvm2)

    }

    //
    //
    // CODE RELATED TO MATCHING THE PATH AND THE QUERY PART OF URIs
    //
    //

    /**
      * Use STRING(s) to match a path segment that is non-empty.
      */
    object STRING {
        def apply(key: String): KeyValueMatcher[String] = (
            key,
            { case Seq(head, _*) ⇒ head }
        )
        def unapply(pathSegment: String): Option[String] = {
            if (!(pathSegment eq null) && pathSegment.length > 0)
                Some(pathSegment)
            else
                None
        }
    }

    object LONG {
        def apply(key: String): KeyValueMatcher[Long] = (
            key,
            new PartialFunction[Seq[String], Long] {
                def isDefinedAt(values: Seq[String]): Boolean = {
                    values.exists(_ match { case LONG(_) ⇒ true; case _ ⇒ false })
                }

                def apply(values: Seq[String]): Long = {
                    values.collectFirst({ case LONG(l) ⇒ l }).get
                }
            }
        )
        def unapply(string: String): Option[Long] = {
            if (string eq null)
                None
            else
                try {
                    Some(java.lang.Long.parseLong(string, 10))
                }
                catch {
                    case e: NumberFormatException ⇒ None
                    case e                        ⇒ throw e;
                }
        }
    }

    object INT {

        def apply(key: String): KeyValueMatcher[Int] = (
            key,
            new PartialFunction[Seq[String], Int] {
                def isDefinedAt(values: Seq[String]): Boolean = {
                    values.exists(_ match { case INT(_) ⇒ true; case _ ⇒ false })
                }

                def apply(values: Seq[String]): Int = {
                    values.collectFirst({ case INT(i) ⇒ i }).get
                }
            }
        )

        def unapply(pathSegment: String): Option[Int] = {
            if (pathSegment eq null) None
            else
                try {
                    Some(java.lang.Integer.parseInt(pathSegment, 10))
                }
                catch {
                    case e: NumberFormatException ⇒ None
                    case e                        ⇒ throw e;
                }
        }
    }

    object bind {

        def path(handlerInitializer: (String) ⇒ Handler): URIMatcher =
            (pathSegment: String) ⇒
                if (pathSegment eq null)
                    None
                else
                    Some((query: String) /* the query string is ignored */ ⇒ Some(handlerInitializer(pathSegment)))

        def query(handlerInitializer: (String) ⇒ Handler): URIMatcher =
            (pathSegment: String) ⇒
                if (pathSegment ne null)
                    None
                else
                    Some((query: String) ⇒ Some(handlerInitializer(query)))
    }

}




