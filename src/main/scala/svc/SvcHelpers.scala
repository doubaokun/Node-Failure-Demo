package svc

import spray.routing.directives.LogEntry
import akka.event.Logging._
import spray.http.HttpRequest

trait SvcHelpers {

  def showRequest(request: HttpRequest) = LogEntry(request.uri, InfoLevel)

  def showRest(request: HttpRequest) = LogEntry(request.uri + " unmatched", WarningLevel)

  val hosturl = Configurator.conf.getString("platform.api.baseurl")

  def hrefBuilder( pathSegments : Any * ) : String = hosturl + "/api/v0/" + pathSegments.mkString("/")


}
