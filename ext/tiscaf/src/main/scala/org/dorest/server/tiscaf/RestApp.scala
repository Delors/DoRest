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
package tiscaf

import zgs.httpd.{ HReqHeaderData, HApp, HReqType }

abstract class RestApp extends HApp {

    private var factories: List[HandlerFactory[_ <: RestLet]] = Nil

    private implicit def tiscaf2dorestMethod(tpe: HReqType.Value): HTTPMethod =
        HTTPMethod(tpe.toString.split("/")(0))

    private class HeaderWrapper(req: HReqHeaderData) {
        def getFirst(key: String) =
            req.header(key).getOrElse("")
    }

    final override def resolve(req: HReqHeaderData) = {

        val path = req.uriPath
        val query = req.query

        println("Handling request at " + new java.util.Date + " :" + path)

        factories.find(_.matchURI(path, query).isDefined).map(_.matchURI(path, query).get)
    }

    def register(handlerFactory: HandlerFactory[_ <: RestLet]) {
        factories = factories.:+(handlerFactory)
    }
    
}
