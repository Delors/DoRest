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

import java.lang.Long

/**
 * @author Michael Eichberg
 */
abstract class HandlerFactory[T <: Handler] {

    implicit def namedToPathSegment(name: String): Named = new Named(name)

    /**
     * A function that does nothing.
     */
    // Used to avoid that we have to deal with Option objects or have to deal with null values.
    def DoNothing(t: T): Unit = {
        /*do nothing*/
    }

    // TODO merge path and pathelement; i.e., make pathelement itself a list
    /**
     *
     * The (URI) path of this resource.
     */
    trait PathMatcher {

        /**
         * Prepends the given path element to this path and returns a new path object.
         */
        def ::(p: PathElement): PathMatcher

        /**
         * Tries to match the path. If the path matches, a list of functions is returned
         * will be called to complete, e.g., the initialization of the resource's parameters.
         */
        def matchSegment(path: String): Option[List[(T) => Unit]]
    }

    class ComplexPath(val pe: PathElement, val tail: PathMatcher) extends PathMatcher {

        def ::(pe: PathElement): PathMatcher = {
            new ComplexPath(pe, this)
        }

        def matchSegment(path: String): Option[List[(T) => Unit]] = {
            pe.matchSegment(path) match {
                case Some((pathRest, f)) => {
                    // this segment matched, but what about the rest of the path?
                    tail.matchSegment(pathRest) match {
                        case Some(fs) => Some(f :: fs)
                        case _ => None
                    }
                }
                case _ => None
            }
        }
    }

    object EmptyPath extends PathMatcher {

        def ::(p: PathElement): PathMatcher = new ComplexPath(p, this)

        def matchSegment(path: String): Option[List[(T) => Unit]] =
            if (path.size == 0) Some(Nil) else None
    }

    trait PathElement {

        /**
         * Constructs a new path that consists of the given path element and this element.
         */
        def ::(ps: PathElement): PathMatcher = {
            new ComplexPath(ps, new ComplexPath(this, EmptyPath))
        }

        /**
         * Tries to match a maximum length segment of the given path. If the match is successful
         * the rest of the path and a function is returned that � if the whole path can be matched �
         * is called.<br>
         * The primary purpose of the function is to enable the initialization of a resource's variable
         * path parameters.
         */
        def matchSegment(path: String): Option[(String, (T) => Unit)]

    }

    /**
     * Can be used to match a path (segment) that is optional and which extends until the end of a given
     * concrete path.
     *
     * If the given path is not empty and does not match the match is considered to have failed
     * unless {link #failOnMatchError} is set to false.
     *
     * Cannot be used to match an optional sub-part of a path. E.g., matching something like
     * [[["/user"::{"/"userid}::"/tag"]]] where [[[{"/userid"}]]] is optional is not possible.
     */
    class Optional(val p: PathMatcher, val failOnMatchError: Boolean) extends PathElement {

        def matchSegment(path: String): Option[(String, (T) => Unit)] = {
            if (path.size == 0) {
                return Some(("", DoNothing))
            }

            p.matchSegment(path) match {
                case Some(fs) => Some(("", (t: T) => {
                    fs.foreach(_(t))
                }))
                case _ => if (failOnMatchError) None else Some(("", DoNothing))
            }
        }

    }

    object Optional {
        def apply(pe: PathElement, failOnMatchError: Boolean = true) = {
            new Optional(new ComplexPath(pe, EmptyPath), failOnMatchError)
        }
    }

    /**
     * Matches a segment that defines a long value.
     */
    class LongValue(val set: (Long) => (T) => Unit) extends PathElement {

        def matchSegment(path: String): Option[(String, (T) => Unit)] = {
            LongValue.matcher.findFirstIn(path).map(s => {
                    (path.substring(s.length), set(s.toLong))
                }
            )
        }
    }

    object LongValue {

        private val matcher = """^-?\d+""".r

        def apply(set: (Long) => (T) => Unit) = new LongValue(set)

        def apply(set: (T,Long) => Unit) = new PathElement {
            def matchSegment(path: String): Option[(String, (T) => Unit)] = {
                LongValue.matcher.findFirstIn(path).map((s) => {
                        (path.substring(s.length), (t : T) => {set(t,s.toLong)})
                    }
                )
            }
        }
    }

    /**
     * Matches a string segment that contains a word character or "@".
     */
    class StringValue(set: (String) => (T) => Unit) extends PathElement {

        def matchSegment(path: String): Option[(String, (T) => Unit)] = {
            StringValue.matcher.findFirstIn(path) match {
                case Some(s) => {
                    Some((path.substring(s.length), set(s)))
                }
                case _ => None
            }
        }
    }

    object StringValue {

        private val matcher = """^(\w|@|-|\.)+""".r

        def apply(set: (String) => (T) => Unit) = new StringValue(set)

    }

    class AnyPath(set: (String) => (T) => Unit) extends PathElement {
        def matchSegment(path: String): Option[(String, (T) => Unit)] = {
            Some("", set(path))
        }
    }

    object AnyPath {
        def apply(set: (String) => (T) => Unit) = new AnyPath(set)
    }

    class Named(val name: String) extends PathElement {

        def matchSegment(path: String): Option[(String, (T) => Unit)] = {
            if (path.startsWith(name)) {
                val pathRest = path.substring(name.length)
                Some((pathRest, DoNothing))
            } else {
                None
            }
        }
    }

    def matchURI(path: String, query: String): Option[T] = {
        this.pathMatcher.matchSegment(path) match {
            case Some(fs) => {
                Some(create(fs))
            }
            case _ => None
        }
    }

    private def create(fs: List[(T) => Unit]): T = {
        val t = create()
        fs.foreach(_(t))
        t
    }

    trait QueryMatcher

    object NoQuery extends QueryMatcher

    private var pathMatcher: PathMatcher = EmptyPath

    def path(f: => PathMatcher) {
        pathMatcher = f
    }

    def path(staticPath: String) {
        path {
            staticPath :: EmptyPath
        }
    }


    private var queryMatcher: QueryMatcher = NoQuery

    def query(f: => QueryMatcher) {
        queryMatcher = f
    }

    /**
     * Creates a new handler object that will be further initialized based on the path matchers.
     */
    def create(): T
}
