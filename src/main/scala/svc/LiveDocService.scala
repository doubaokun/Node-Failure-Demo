package svc

import akka.event.Logging._
import spray.routing.HttpService
import spray.http.MediaTypes._
import spray.http.{HttpRequest, StatusCodes}
import StatusCodes._
import spray.routing.directives.CachingDirectives._
import spray.httpx.encoding.Gzip
import spray.routing.directives.LogEntry
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext

trait LiveDocService extends HttpService {


  val simpleCache = routeCache(maxCapacity = 1000, timeToIdle = Duration("30 min"))

  val docRoute = {
    path("doc"){
      redirect("doc/v0", Found) // point to latest
    } ~
    pathPrefix( "doc" / "v0" / "api" ~ Slash.? ) {
     // logRequest(showRequest _) {
        cache(simpleCache) {
          getFromDirectory("./src/main/webapp/apiv0/doc/api/")
        }
      //}
    } ~
    path( "doc" / "v0" ~ Slash.?) {
      //logRequest(showRequest _) {
        cache(simpleCache) {
          getFromFile("./src/main/webapp/apiv0/doc/apidoc.html")  // serve from the file structure for now - TODO: add to jar and serve with getFromResource
        }
      //}
    } ~
      pathPrefix("style") {
        cache(simpleCache) {
          getFromDirectory("./src/main/webapp/style")
        }
      } ~
      pathPrefix("style/images") {
        cache(simpleCache) { getFromDirectory("./src/main/webapp/style/images") }
      } ~
      pathPrefix("classpath") {
        cache(simpleCache) { getFromDirectory("./src/main/webapp/classpath") }
      } ~
      pathPrefix("css") {
        cache(simpleCache) { getFromDirectory("./src/main/webapp/css") }
      } ~
      pathPrefix("js") {
        cache(simpleCache) { getFromDirectory("./src/main/webapp/js") }
      } ~
      pathPrefix("img") {
        cache(simpleCache) { getFromDirectory("./src/main/webapp/img") }
      } ~
      path(Rest) {
        leftover => {
        //  logRequest(showRest _ ) {
            encodeResponse(Gzip) {
              getFromDirectory("./src/main/webapp/")
            }
          //}
        }
      }

  }


}
