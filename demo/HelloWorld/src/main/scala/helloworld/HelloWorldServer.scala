/*
   Copyright 2011,2012 Michael Eichberg et al

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
package helloworld

import org.dorest.server.jdk.JDKServer
import org.dorest.server.rest.RESTInterface
import org.dorest.server.rest.TEXTSupport

object HelloWorldServer extends JDKServer(9010) with App {

    addPathMatcher(
        / {
            case "hello" ⇒ new RESTInterface with TEXTSupport {
                get returns TEXT { "Hello World!" }
            }

            case "echo" ⇒ / {
                case MATCHED() ⇒ new RESTInterface with TEXTSupport {
                    get returns TEXT { "This is the echo service." }
                }
                case STRING(text) ⇒ new RESTInterface with TEXTSupport {
                    get returns TEXT { text }
                }
            }
        }
    )

    start()
}
