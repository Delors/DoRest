package org.dorest.server.cookies

import org.dorest.server.jdk.JDKServer
import org.dorest.server.log.ConsoleLogging
import org.dorest.server.rest.RESTInterface
import org.dorest.server.rest.HTMLSupport


/**
 * This is an example for recognizing users via cookie.
 * It demonstrates how to access an existing cookie and how to set a new cookie.
 * 
 * About the application:
 * If the application knows the user it greets him.
 * If not, it will ask him for his name. The name is stored as cookie and is available for 60 seconds.
 * 
 * 
 * How to use this demo:
 * Run the GreetingDemo object as scala application.
 * It will start a web server at port 9011.
 * 
 * Open the web page at http://localhost:9011/index
 */
object GreetingDemo extends JDKServer(9011)
    with ConsoleLogging
    with App {
    val userHomeDir = System.getProperty("user.home")

    addURIMatcher(
        / {
            case MATCHED() => new NameResource
            case "index" => new NameResource
            case "setname" => new SetNameResource
        })

    start()

}

/**
 * This is the entry point for the web page. It greets users or asks them for their name.
 */
class NameResource extends RESTInterface with HTMLSupport with Cookies {
    get returns HTML {
        //try to find the cookie that contains the user name
        val nameCookie = cookie("name")
        nameCookie headOption match {
            case None => 	// Can't find it, request the user name in order to store it
                			requestName.toString 
                			
            case Some(nameCookie) => 	// Found the cookie. Now greet the user.
                			printWelcome(nameCookie value).toString 
        }
    }

    def requestName = {
        <html>
            <head><title>I don't know you yet!</title></head>
            <body>
                Please enter your name<form action="setname" method="get">
                                          <input name="name" type="text" size="30" maxlength="30"/>
                                          <input type="submit" value="Confirm"/>
                                      </form>
            </body>
        </html>
    }

    def printWelcome(name: String) = {
        <html>Hi { name }!</html>
    }
}


/**
 * This resource is used to set the cookie that contains the user name.
 */
class SetNameResource extends RESTInterface with HTMLSupport with Cookies {
    get returns HTML {
        //the user name is given as get parameter. Extract it from the request url.
        val name = extractNameFromUrl
        
        //now set a cookie with the extracted name
        set cookie "name" value name path "/index" maxAge 60
        
        <html>
            <body>
                Your name will be available via cookie for the next 60 seconds!<br/>
                <a href="/index">Return to main page</a>
            </body>
        </html>.toString
    }
    
    def extractNameFromUrl={
        val query = this.requestURI.getQuery()
        query.substring(query.indexOf("=") + 1)
    }
}