/*
   Copyright 2012 Michael Eichberg et al

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
package jdk

import log._

import com.sun.net.httpserver._

import java.net._
import javax.net.ssl._
import java.security.KeyStore
import java.io.InputStream

import java.util.concurrent.Executor
import java.util.concurrent.Executors

/** @author Michael Eichberg (supported by students of the lecture Enterprise Application Design 2012, Technische Universität Darmstadt)
  */
class HttpsJDKServer(protected[this] val server: HttpsServer) extends CommonJDKServer[HttpsServer] {

    def this(port: Int) {
        this(HttpsServer.create(new InetSocketAddress(port), 0))
    }

    private var configured = false;

    protected[this] val logger = Logger(classOf[HttpsJDKServer])

    final def configSSL(config: (Array[javax.net.ssl.KeyManager], Array[javax.net.ssl.TrustManager])) {
        var (kms, tms) = config
        configSSL(kms, tms)
    }

    def configSSL(keyManagers: Array[javax.net.ssl.KeyManager], trustManagers: Array[javax.net.ssl.TrustManager]) {
        var sslContext: SSLContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagers, trustManagers, null);

        server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
            override def configure(params: HttpsParameters) {
                var context = SSLContext.getDefault();
                var engine = context.createSSLEngine();
                params.setNeedClientAuth(false);
                params.setCipherSuites(engine.getEnabledCipherSuites());
                params.setProtocols(engine.getEnabledProtocols());
                params.setSSLParameters(context.getDefaultSSLParameters());
            }
        });

        configured = true
    }

    override def start(executor: Executor = Executors.newCachedThreadPool()) {
        if (!configured)
            throw new IllegalStateException("the server needs to be configured (\"configSSL\") before it can be started")
        super.start(executor)

    }
}

object HttpsJDKServer {

    def setupKeystore(keystore: URL, password: Array[Char]): (Array[javax.net.ssl.KeyManager], Array[javax.net.ssl.TrustManager]) = {
        val in = keystore.openStream();
        try {
            setupKeystore(in, password)
        }
        finally {
            if (!(in eq null))
                in.close()
        }
    }

    def setupKeystore(keystore: java.io.InputStream, password: Array[Char]): (Array[javax.net.ssl.KeyManager], Array[javax.net.ssl.TrustManager]) = {

        val ks = KeyStore.getInstance("JKS");
        ks.load(keystore, password);

        val kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, password);

        val tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        (kmf.getKeyManagers(), tmf.getTrustManagers())
    }

}