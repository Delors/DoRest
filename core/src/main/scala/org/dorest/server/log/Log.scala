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
package log

sealed trait LogLevel

/**
 * Message to indicate that something is really wrong.
 */
case object SEVERE extends LogLevel

/**
 * Messages that indicate that something is wrong, but which do not pose a general thread to the operation of the
 * server.
 */
case object WARN extends LogLevel

/**
 * Messages that are relevant to understand the operation of the server.
 */
case object INFO extends LogLevel

/**
 * Stuff that are only relevant during development time.
 */
case object DEBUG extends LogLevel


trait Log /* TODO implement a LogProvider */ {


    /**
     * Example Scenario:
     * {{{
     * class MyServer {
     *  def handleRequest() {
     *      log(INFO){"Available memory: "+...} // the result will only be constructed if INFO message should be logged.
     *      log[MyServer](INFO){"Handling request"}
     *  }
     * }
     * }}}
     * @type T if specified, the log message will include the (compile-time) type of specified class.
     */
    // TODO (How) Can we bind T to the current class in which the call is made?
    def log[T](level: LogLevel)(message: => String)(implicit clazz: scala.reflect.ClassManifest[T]) {
        if (clazz != ClassManifest.Nothing)
            print(clazz.toString + ": ")
        println(level + ": " + message)
    }

    def log[T](level: LogLevel,exception: Throwable)(implicit clazz: scala.reflect.ClassManifest[T]) {
        if (clazz != ClassManifest.Nothing)
            print(clazz.toString + ": ")
        println(level + ": " + exception.toString)
    }


}