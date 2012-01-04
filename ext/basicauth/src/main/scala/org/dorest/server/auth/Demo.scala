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
package auth

import utils._
import jdk._
import log._
import rest._

/**
 * Demonstrates how to set up a resource that requires http basic authentication.
 *
 * If you require the authentication of the user against, e.g. a database, just mix in an appropriate trait instead
 * of the {{{SimpleAuthenticator}}} trait.
 */
trait Authorization
  extends BasicAuthentication
   {

  def authenticationRealm = "Demo App"
    
  /**
   * If you require the authentication of the user against, e.g. a database, just mix in an appropriate trait
   * which implements the {{{Authentication}}} trait including this method.
   */
  def password(username: String): Option[String] = {
    username match {
      case "max" => Option("safe")
      case _ => None
    }
  }

}

/**
 * Before a request will be handled by this Time resource, the performance monitor and the authorization traits
 * are both triggered. I.e., the performance is measured even if the authorization fails. If you change the order
 * in which you mix in the traits, e.g., as in the following example:
 * {{{
 *   class Time
 *      extends RESTInterface
 *      with PerformanceMonitor
 *      with Authorization
 *      with TEXTSupport
 *      with HTMLSupport
 *      with XMLSupport {
 * }}}
 * The performance is only measured if the user is successfully authorized. The order and position of the
 * {{{Support}}} traits is not further relevant because they do not override/extend the process handling. They
 * basically just complement the possibilities offered by the Time resource.
 */
class Time
  extends RESTInterface
  with ConsoleLogging
  with Authorization
  with PerformanceMonitor
  with TEXTSupport
  with HTMLSupport
  with XMLSupport {

  val dateString = new java.util.Date().toString

  get returns TEXT {
    dateString
  }

  get returns HTML {
    "<html><body>The current (server) time is: " + dateString + "</body></html>"
  }

  get returns XML {
    <time>{ dateString }</time>
  }
}

class Info extends RESTInterface with Authorization with TEXTSupport {

  var info: String = _

  get returns TEXT {
    "Info: " + info + " [user=" + authenticatedUser.get + "]"
  }
}

class Demo

object Demo
  extends Server(9000)
  with ConsoleLogging
  with App {

  register(new HandlerFactory[Time] {
    path {
      "/time"
    }

    def create = new Time()
  })

  register(new HandlerFactory[MappedDirectory] {

    val userHomeDir = System.getProperty("user.home")

    path {
      "/static" :: AnyPath(v => _.path = if (v startsWith "/") v else "/" + v)
    }

    def create = new MappedDirectory(userHomeDir) with Authorization
  })

  this register new HandlerFactory[Info] {
    path {
      "/info/" :: StringValue(_.info = _)
    }

    def create = new Info with PerformanceMonitor with ConsoleLogging // TODO needs to exchanged
  }

  start()
}


