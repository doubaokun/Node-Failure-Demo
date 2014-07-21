package svc

import java.security.{SecureRandom, KeyStore}
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}
import spray.io._

/**
 * May need to install unlimited strength crypto:
 * http://www.oracle.com/technetwork/java/javase/downloads/jce-7-download-432124.html
 */
trait SslConfiguration {

  implicit def sslContext: SSLContext = {
    val passwd = "cotspass101".toCharArray
    val jks = "/sslmodelergyguest.jks"
    val keyStore = KeyStore.getInstance("jks")
    keyStore.load(getClass.getResourceAsStream(jks), passwd)
    val keyManagerFactory = KeyManagerFactory.getInstance("SunX509")
    keyManagerFactory.init(keyStore, passwd)
    val trustManagerFactory = TrustManagerFactory.getInstance("SunX509")
    trustManagerFactory.init(keyStore)
    val context = SSLContext.getInstance("TLS")
    context.init(keyManagerFactory.getKeyManagers, trustManagerFactory.getTrustManagers, new SecureRandom)
    context
  }

  implicit val myEngineProvider = ServerSSLEngineProvider { engine =>
    engine.setEnabledCipherSuites(Array("TLS_RSA_WITH_AES_256_CBC_SHA"))
    engine.setEnabledProtocols(Array("SSLv3", "TLSv1"))
    engine
  }

}




// Place a special SSLContext in scope here to be used by HttpClient.
// It trusts all server certificates.
//implicit def trustfulSslContext: SSLContext = {
//object BlindFaithX509TrustManager extends X509TrustManager {
//  def checkClientTrusted(chain: Array[X509Certificate], authType: String) = ()
//  def checkServerTrusted(chain: Array[X509Certificate], authType: String) = ()
//  def getAcceptedIssuers = Array[X509Certificate]()
//}
//
//val context = SSLContext.getInstance("TLS")
//context.init(Array[KeyManager](), Array(BlindFaithX509TrustManager), null)
//context
//}
