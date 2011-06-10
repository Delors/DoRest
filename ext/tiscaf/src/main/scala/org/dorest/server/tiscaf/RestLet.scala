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

import java.net.URI
import java.io.ByteArrayInputStream

import rest.RESTInterface
import zgs.httpd._

/**
 *
 * This HLet allows to use the DoRest API in tiscaf.
 *
 * @author Lucas Satabin
 *
 */
trait RestLet extends RESTInterface with HLet {

    private implicit def tiscaf2dorestMethod(tpe: HReqType.Value): HTTPMethod.Value =
        HTTPMethod.withName(tpe.toString.split("/")(0))

    private class HeaderWrapper(talk: HTalk) {
        def getFirst(key: String) =
            talk.req.header(key).getOrElse("")
    }

    final override def act(talk: HTalk) {
        // initialize this handler
        this.protocol = talk.req.protocol
        this.method = talk.req.method
        this.remoteAddress = talk.req.remoteIp
        this.requestURI =
            new URI("", "", talk.req.host.getOrElse(""),
                talk.req.port.map(_.toInt).getOrElse(0),
                talk.req.uriPath, talk.req.query, "")
        this.requestHeaders = new HeaderWrapper(talk)

        val stream = talk.req.octets match {
            case Some(bytes) => new ByteArrayInputStream(bytes)
            case _ => null
        }
        val response = this.processRequest(stream)
        
        talk.
          setStatus(HStatus.fromInt(response.code)).
          setContentLength(response.body.length)
          
        response.headers.foreach {
            case (key, value) => talk.setHeader(key, value)
        }
//        talk.write(reponse.responseBody)
        
        talk.close
        
//        try {
//            val length = response.body.length
//            response.headers.foreach((header) => { val (key, value) = header; t.getResponseHeaders().set(key, value) })
//            t.sendResponseHeaders(response.code, length);
//            if (length > 0) {
//                response.body.write(t.getResponseBody())
//            }
//            t.close();
//        } catch {
//            case ex => {
//                ex.printStackTrace()
//            }
//        } finally {
//            // we were able to handle the request..
//            return ;
//        }

    }

}
