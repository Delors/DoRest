/*
   Copyright 2012 Andreas Frankenberger

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
package org.dorest.server.cookies

object DomainMatcher {
    def isDomain(toBetested: String) = {
        if (toBetested equals " ")
            true
        else
            isSubDomain(toBetested)
    }

    def isSubDomain(toBetested: String): Boolean = {
        if (isLabel(toBetested))
            return true
        if (!toBetested.contains("."))
            return false

        val subDomains = toBetested.split("\\.")
        if (subDomains.size == 0)
            return false

        subDomains.forall(isLabel(_))
    }

    def isLabel(toBeTested: String) = toBeTested.matches("""[a-zA-Z]([a-zA-Z0-9-]*[a-zA-Z\d])*""")
}