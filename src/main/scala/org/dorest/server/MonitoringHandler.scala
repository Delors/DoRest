package org.dorest.server

import java.net._
import java.io._

trait MonitoringHandler extends Handler {

    override abstract def processRequest(requestBody : InputStream) : Response = {
        val startTime = System.nanoTime
        val response = super.processRequest(requestBody)
        val endTime = System.nanoTime
        println("Time to process the request: "+(endTime - startTime)+" nanoseconds")
        response
    }
}