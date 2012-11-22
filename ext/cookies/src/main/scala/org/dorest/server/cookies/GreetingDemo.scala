package org.dorest.server.cookies

import org.dorest.server.jdk.JDKServer
import org.dorest.server.log.ConsoleLogging
import org.dorest.server.rest.RESTInterface
import org.dorest.server.rest.HTMLSupport

class NameResource extends RESTInterface with HTMLSupport with Cookies{
	get returns HTML {
	  cookie("name") headOption match {
	    case None => requestName.toString
	    case Some(nameCookie) => printWelcome(nameCookie value).toString
	  }
	}
	
	def requestName={
	  <html><head><title>I don't know you yet!</title></head>
<body>Please enter your name<form action="setname" method="get">
<input name="name" type="text" size="30" maxlength="30" />
<input type="submit" value="Confirm"/>
</form></body></html>
	}
	
	def printWelcome(name:String)={
	  <html>Hi {name}!</html>
	}
}

class SetNameResource extends RESTInterface with HTMLSupport with Cookies{
  get returns HTML{
    val query=this.requestURI.getQuery()
    
    val name=query.substring(query.indexOf("=")+1)
    set cookie "name" value name path "/index" maxAge 60
    <html><body><a href="/index">Return to main page</a></body></html>.toString
  }
}

object GreetingDemo extends JDKServer(9011)
        with ConsoleLogging
        with App{
  val userHomeDir = System.getProperty("user.home")

    addURIMatcher(
        / { case MATCHED() => new NameResource
        case "index" => new NameResource
        case "setname" => new SetNameResource
        }
    )

    start()

}