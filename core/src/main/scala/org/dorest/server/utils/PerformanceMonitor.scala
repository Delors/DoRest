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
package utils

import log.INFO
import log.LogLevel

import java.io._

/**
 * Measures the time it takes to process a request. This time never takes into account the time required by the framework to identify the correct handler and parse the URL; it's just the time required to create the representation.
 *
 * @author Michael Eichberg
 */
trait PerformanceMonitor extends Handler {

    def log[T](level : LogLevel)(message : => String)(implicit clazz : scala.reflect.ClassManifest[T]) : Unit

    override abstract def processRequest(requestBody : InputStream) : Response = {
        val startTime = System.nanoTime
        try {
            val response = super.processRequest(requestBody)
            response
        } finally {
            val endTime = System.nanoTime

            log[this.type](INFO){ "Time to process the request: "+(endTime - startTime)+" nanoseconds" }
        }
    }
}