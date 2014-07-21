package svc

import com.typesafe.scalalogging.slf4j.Logging
import com.typesafe.config.ConfigFactory
import org.apache.commons.lang.StringUtils


object Configurator extends Logging {

  val conf = ConfigFactory.load( s"${appHome}/${envName}/platform.svc" )
    .withFallback(ConfigFactory.load("./platform.svc"))
    .withFallback(ConfigFactory.load("platform.svc"))

  conf.checkValid(ConfigFactory.load("platform.svc"),"platform")
  logger.info( s"Server Mode ${conf.getString("platform.api.runmode")} ")
  //logger.debug( conf.entrySet().toString )


  lazy val appHome = {
    System.getenv().get("SE_HOME") match {
      case null => {
        logger.warn("appHome system property undefined - assuming default CWD")
        System.getProperty("se.home") match {
          case prop if StringUtils.isNotBlank(prop) => System.setProperty("SE_HOME",prop)
          case _ => "."
        }
      }
      case env => env
    }
  }

  lazy val envName = {
    System.getenv().get("SE_ENV") match {
      case null => {
        logger.warn("envName system property undefined - assuming default runtime environment 'testconf'")
        System.getProperty("se.env") match {
          case prop if StringUtils.isNotBlank(prop) => System.setProperty("SE_ENV",prop)
          case _ => "testconf"
        }
      }
      case env => env
    }
  }
}

