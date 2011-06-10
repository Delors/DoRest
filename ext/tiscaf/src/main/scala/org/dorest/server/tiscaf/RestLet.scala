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

import rest.RESTInterface
import zgs.httpd.{HLet,HTalk,HReqType}

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
    
    final override def act(talk: HTalk) {
        // initialize this handler
    }
    
}
