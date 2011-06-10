package org.dorest.demo

import org.dorest.server._
import org.dorest.server.rest._
import org.dorest.server.auth._
//import org.json._

// http://www.scala-lang.org/api/current/index.html#package

class Main
object Main extends Server(9000) with App {
/*
    this register new HandlerFactory[User] {
        path { "/user/" :: StringValue((v) => _.user = v) }
        def create = new User with MonitoringHandler
    }*/

    register(new HandlerFactory[Time] {
        path { "/time" :: EmptyPath }
        query { NoQuery } // ("timezone",StringValue(v => _.timeZone = v)) 
        def create = new Time() with MonitoringHandler
    })

  /*  register(new HandlerFactory[Tags] {
        path { "/tags" :: Optional("/") }
        def create = new Tags
    })

    register(new HandlerFactory[Tag] {
        path { "/tags/" :: LongValue((v) => (r) => r.tagId = v) }
        def create = new Tag
    })
*/
    register(new HandlerFactory[MappedDirectory] {
        path { "/webapp" :: AnyPath(v => _.path = { if (v startsWith "/") v else "/"+v }) }
        def create = new MonitoredMappedDirectory("/Users/Michael")
    })

/*    register(new HandlerFactory[Note] {
        path { "/tags/" :: LongValue((v) => _.tagId = v) :: "/notes/" :: LongValue((v) => _.noteId = v) }
        def create = new Note
    })
*/
    start()
}


trait Authorization
        extends BasicAuthentication
        with SimpleAuthenticator
        with AuthenticatedUser {

    def authenticationRealm = "Demo App"
    val authorizationUser = "user"
    val authorizationPwd = "safe"
}


class Time
        extends RESTInterface
        with Authorization
        with MonitoringHandler
        with TEXTSupport
        with HTMLSupport
        with XMLSupport {

    //var timeZone: String = _
    //java.util.Calendar()

    val dateString = new java.util.Date().toString

/*
    get requests JSON {
        val jo = new JSONObject()
        jo.put("time", dateString)
        jo
    }
*/
    get requests TEXT {
        dateString
    }

    get requests HTML {
        "<html><body>The current (server) time is: "+dateString+"</body></html>"
    }

    get requests XML {
        <time>{ dateString }</time>
    }
}

/**
 * This class tests the very core part of the framework!
 *
 * Enables you to make the content of a directory available.
 */
class MappedDirectory(val baseDirectory: String) extends Handler {

    import java.io._

    var path: String = _

    def processRequest(requestBody: InputStream): Response = {
        if (method != GET) {
            return new SupportedMethodsResponse(GET)
        }

        val file = new File(baseDirectory+"/"+path)
        if (!file.exists) {
            return NotFoundResponse
        }

        if (file.isDirectory) {
            return new Forbidden("Browsing directories is forbidden.")
        }

        new OkResponse {
            // we ignore the accept header for now
            // TODO encoding... etc. 
            val headers = new DefaultResponseHeaders
            val body = new ResponseBody {

                lazy val contentType = {
                    val fileName = file.getName()
                    Some((fileName.substring(fileName.lastIndexOf('.')) match {
                        case "css"  => MediaType.CSS
                        case "js"   => MediaType.JAVASCRIPT
                        case "html" => MediaType.HTML
                        case "xml"  => MediaType.XML
                        case "txt"  => MediaType.TEXT
                        case _      => throw new Error("Media type detection based on file suffix failed: "+fileName)
                    },
                        // We are not able to determine the used charset..
                        None))
                }

                def length = file.length.asInstanceOf[Int]

                def write(responseBody: OutputStream) {

                    // TODO use apache.commons.io...
                    val in = new FileInputStream(file)
                    try {
                        while (in.available > 0)
                            responseBody.write(in.read)
                    } finally {
                        if (in != null)
                            in.close
                    }
                }
            }
        }
    }
}

class MonitoredMappedDirectory(baseDirectory: String)
    extends MappedDirectory(baseDirectory)
    with MonitoringHandler

