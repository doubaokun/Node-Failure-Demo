package svc

import akka.actor.{Props, Actor}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import mdl.JsonFormats
import spray.routing._
import spray.http._
import MediaTypes._
import StatusCodes._
import org.json4s.{native, NoTypeHints}
import org.json4s.native.Serialization.{read,write}
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization
import mdl._
import akka.actor.{Actor, ActorSystem, Props, RootActorPath}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps


import akka.event.Logging
import spray.routing.directives.DebuggingDirectives
import akka.routing.FromConfig
import akka.actor.ActorSystem

import scala.Some
import scala.concurrent.{Await, Future}

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor

class SaaServiceActor extends Actor with SaaService
                                    with LiveDocService
                                     {

  override implicit val formats = baseformats

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(docRoute ~ apiRoute)
  //val prducer  = context.actorSelection("akka.tcp://ClusterSystem@127.0.0.1:2554/producer")
}

class intermediate extends Actor
{

  def receive =  {
    case "producer" => sender ! context.actorSelection("akka.tcp://ClusterSystem@127.0.0.1:2554/producer")
  }
}

// this trait defines our service behavior independently from the service actor
trait SaaService extends HttpService with SvcHelpers with JsonFormats {
  implicit def executionContext = actorRefFactory.dispatcher

  implicit val formats = baseformats

  //val cloudRouter  = ActorSystem  ("modelergy-ptf-run").actorOf(Props[SimConductor].withRouter(FromConfig()), "platform-cloud-router1")

  val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=2554").
    withFallback(ConfigFactory.parseString("akka.cluster.roles = [producer]")).
    withFallback(ConfigFactory.load())
  val system = ActorSystem("ClusterSystem", config)
  val producer = system.actorOf(Props[GradeGenerator], name = "producer")


    /*val config2 = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=2553").
    withFallback(ConfigFactory.parseString("akka.cluster.roles = [consumer]")).
    withFallback(ConfigFactory.parseString(
    """
      |akka.persistence.journal.plugin = "cassandra-journal"
      |akka.persistence.snapshot-store.plugin = "cassandra-snapshot-store"
    """.stripMargin)).
    withFallback(ConfigFactory.load())
  val system2 = ActorSystem("ClusterSystem", config2)
  val consumer = system2.actorOf(Props[GPAInterface], name = "consumer")*/


 // val consumer = system.actorSelection("akka.tcp://ClusterSystem@127.0.0.1:2553/user/consumer")


 /* val config3 = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=2555").
    withFallback(ConfigFactory.parseString("akka.cluster.roles = [producer]")).
    withFallback(ConfigFactory.load())
  val system3 = ActorSystem("ClusterSystem", config3)
  val producer2 = system3.actorOf(Props[GradeGenerator], name = "producer")*/



  val hyperdocL0V0 =
    <html>
      <body>
        <section>
          <h2>Available endpoints:</h2>
          <ul>
            <li><a href="sim"> <strong>/sim/</strong> : Simulation definitions and management</a> </li>
            <li><a href="usr"><strong>/usr/</strong> : User definitions and management</a> </li>
            <li><a href="grp"><strong>/grp/</strong> : User Group definitions and management</a> </li>
          </ul>
        </section>

        <section>
          <h2>Documentation for humans</h2>
          <a href="/doc/v0/"> <strong>/doc/v0/</strong> : Live API documentation</a>
        </section>
      </body>
    </html>

  val apiRoute =
    path("" ~ Slash.?){
      redirect("doc", Found)
    } ~
    path("api" ~ Slash.?) {
      redirect("/api/v0", Found)
    } ~
    pathPrefix("api" / "v0" ) {
      path("" ~ Slash.?) {
        complete {
          hyperdocL0V0
        }
      } ~
        path("doc") {
          redirect("/doc/v0/", Found)
        } ~
        path("sims" ~ Slash.?) {
          get {
            DebuggingDirectives.logRequest("get-sim-list", Logging.DebugLevel) {
              respondWithMediaType(`application/json`) {
                complete("What is you name my Friend")
                //Cluster.get(ClusterSystem).leave()

              }
            }
          } ~
            put {
              respondWithStatus(StatusCodes.NotImplemented) {
                //complete( """{"msg:"not implemented yet: please use api/v0/sim/ for individual entries. This endpoint will allow bulk upload and merge of simulation definitions.}""")
                complete("Taamneh---------Kareemmmmmmmmmmmm")
              }
            }
        } ~
        pathPrefix("sim") {
          pathEndOrSingleSlash {
            get {
              entity(as[String]) { sim_str =>
              //DebuggingDirectives.logRequest("get-sim-list", Logging.DebugLevel) {
                respondWithMediaType(`application/json`) {
                  implicit val timeout = Timeout(5 seconds)
                  val future: Future[Any] = producer.ask("GetNodes")(5 seconds) // enabled by the “ask” import
                  val result = Await.result(future, timeout.duration).asInstanceOf[String]
                  complete(result)
                }

                }
              //}            }
          } ~
            put {
              entity(as[String]) { sim_str =>
                //putSim(sim_str) { (code, sim) =>
                //respondWithHeader(HttpHeaders.Location(hrefBuilder("sim", sim.id))) {
                respondWithMediaType(`application/json`) {
                  println("TEST@@@@@@@@@@@@@@@@@@@@@@@@")
                  /*val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=2553").
                        withFallback(ConfigFactory.parseString("akka.cluster.roles = [consumer]")).
                        withFallback(ConfigFactory.parseString(
                        """
                          |akka.persistence.journal.plugin = "cassandra-journal"
                          |akka.persistence.snapshot-store.plugin = "cassandra-snapshot-store"
                        """.stripMargin)).
                        withFallback(ConfigFactory.load())
                      val system = ActorSystem("ClusterSystem", config)
                      val consumer = system.actorOf(Props[GPAInterface], name = "consumer")*/
                  //consumer !Grade(sim_str.toInt, 3)
                  producer ! sim_str.toInt
                  complete("The GPA has been updated")

                }
              }
            }
                //}
             // }
            } ~
            path(Segment) { simid =>
              get {
                DebuggingDirectives.logRequest(s"get-sim-${simid}", Logging.DebugLevel) {
                  //DataStoreAccess.ds.getSim(simid) match {
                    //case Some(sim) => {
                      respondWithMediaType(`application/json`) {
                        /*implicit val timeout = Timeout(5 seconds)
                        val future: Future[Any] = consumer.ask("Request")(5 seconds) // enabled by the “ask” import
                        val result = Await.result(future, timeout.duration).asInstanceOf[Double]
                        complete("Current Persistance.GPA:" + result)*/
                        implicit val timeout = Timeout(5 seconds)
                        val future: Future[Any] = producer.ask("Request")(5 seconds) // enabled by the “ask” import
                        val result = Await.result(future, timeout.duration).asInstanceOf[Double]
                        complete("Current Persistance.GPA:" + result)

                      }
                    //}
                    //case None => respondWithStatus(StatusCodes.NotFound) {
                      //complete(s"No simulation metadata found for ${simid}. Please use the sim search API to find the correct identifier")
                    //}
                  }
                }
              } ~
                put {
                  entity(as[String]) { sim_str =>
                    try {
                      //DataStoreAccess.ds.putSim(read[Sim](sim_str)) match {
                        //case (overwrite, sim, id) if overwrite => {
                          respondWithStatus(StatusCodes.AlreadyReported) {
                            respondWithMediaType(`application/json`) {
                              complete("KKKKKKKKKKKKKKKKKKKK")
                            }
                          //}
                        //}

                      }
                    } catch {
                      case ex: Exception => respondWithStatus(StatusCodes.NotAcceptable) {
                        complete("Could not parse simulation metadata document")
                      }
                    }
                  }
                }
            } ~
      pathPrefix("run" / Segment ) { simid =>
        pathEndOrSingleSlash {
          get {
            respondWithMediaType(`application/json`) {
              //DataStoreAccess.ds.getRunDoc(simid) match {
                //case Some(rundoc) => complete(rundoc)
                //case None => respondWithStatus(StatusCodes.NotFound) {
                  //complete(s"Could not find and run instances for ${simid}")
              complete("KKKKKKKKKKKKKKKKKKKK")
                //}
              //}
            }
          } ~
            post {
              entity(as[String]) {
                noopDoc =>
                  //respondWithMediaType(`application/json`) {
                    //val runid = DataStoreAccess.ds.nextRunId(simid)
                    //DataStoreAccess.ds.putRun(simid, runid) match {
                      //case Some(s) =>
                        //cloudRouter ! RunIt(simid, runid)
                        //respondWithHeader(HttpHeaders.Location( hrefBuilder("run",simid,runid) )) {
                          ///respondWithStatus(StatusCodes.Accepted) {
                            //complete( s""" {"href":${hrefBuilder("run",simid,runid)}"}""" )
                          //}
                        //}
                      //case None => respondWithStatus(StatusCodes.BadRequest) {
                        //complete("Could not add ${simid} to the runlist")
                  complete("KKKKKKKKKKKKKKKKKKKK")
                      //}
                    //}
                  //}
              }
            }
        } ~
          path( IntNumber ~ Slash.?) { runid =>
            get {
              //respondWithMediaType(`application/json`) {
                //DataStoreAccess.ds.getRun(simid,runid) match {
                  //case Some(simrun) => complete(pretty(render(parse(write(simrun)))))
                  //case None => respondWithStatus(StatusCodes.NotFound) {
                    //complete( s""" {"msg":"NotFound status of sim : ${simid} instance: ${runid}"} """ )
              complete("KKKKKKKKKKKKKKKKKKKK")
                  //}
                //}
              //}
            } ~
              post {
                parameter('command) { command => {
                  command match {
                    case "shutdown" => {
                      //cloudRouter ! StopIt(simid, runid)
                      //respondWithMediaType(`application/json`) {
                        //respondWithStatus(StatusCodes.Accepted) {
                          //complete( s""" {"msg":"sent shutdown command to : ${simid} instance: ${runid} "}""")
                  complete("KKKKKKKKKKKKKKKKKKKK")
                        }
                      //}
                    //}
                   // case bc => {
                     // respondWithMediaType(`application/json`) {
                       // respondWithStatus(StatusCodes.BadRequest) {
                          //complete( s""" {"msg":"bad command : ${bc} command to : ${simid} instance: ${runid} "}""")
                  complete("KKKKKKKKKKKKKKKKKKKK")
                        //}
                      //}
                    //}
                  }
                }
                }
              }
          }
      }
        }






          /*~
          pathPrefix("api" / "v0" ) {
            path("" ~ Slash.?){
              complete { hyperdocL0V0 }
            } ~
            path("doc"){
              redirect("/doc/v0/", Found)
            } ~
            path("sims" ~ Slash.?) {
              get {
                DebuggingDirectives.logRequest("get-sim-list", Logging.DebugLevel) {
                  respondWithMediaType(`application/json`) {
                    getSimListSummary("dummyuser - sims") { userx =>
                      complete(userx)
                    }
                  }
                }
              } ~
              put {
                respondWithStatus(StatusCodes.NotImplemented) {
                  complete("""{"msg:"not implemented yet: please use api/v0/sim/ for individual entries. This endpoint will allow bulk upload and merge of simulation definitions.}""")
                }
              }
            } ~
            pathPrefix("sim") {
              pathEndOrSingleSlash {
                get {
                  DebuggingDirectives.logRequest("get-sim-list", Logging.DebugLevel) {
                    respondWithMediaType(`application/json`) {
                      getSimListSummary("dummyuser - sim") { userx =>
                        complete(userx)
                      }
                    }
                  }
                } ~
                put {
                  entity(as[String]) { sim_str =>
                    putSim(sim_str) { ( code, sim ) =>
                      respondWithHeader(HttpHeaders.Location( hrefBuilder("sim",sim.id) )) {
                        respondWithMediaType(`application/json`) {
                          complete( code, pretty(render(parse(write(sim)))))
                        }
                      }
                    }
                  }
                }
              } ~
              path( Segment ){ simid =>
                get {
                  DebuggingDirectives.logRequest(s"get-sim-${simid}", Logging.DebugLevel) {
                    DataStoreAccess.ds.getSim(simid) match {
                      case Some(sim) => {
                        respondWithMediaType(`application/json`) {
                          complete( pretty(render(parse(write(sim)))))
                        }
                      }
                      case None => respondWithStatus( StatusCodes.NotFound ){ complete(s"No simulation metadata found for ${simid}. Please use the sim search API to find the correct identifier") }
                    }
                  }
                } ~
                put {
                  entity(as[String]) { sim_str =>
                    try {
                      DataStoreAccess.ds.putSim(read[Sim](sim_str)) match {
                        case (overwrite, sim, id) if overwrite => {
                           respondWithStatus(StatusCodes.AlreadyReported){
                             respondWithMediaType(`application/json`) {
                                 complete( pretty(render(parse(write(sim)))))
                             }
                           }
                        }
                        case (overwrite, sim, id) if !overwrite => {
                          respondWithStatus(StatusCodes.Accepted){
                            respondWithMediaType(`application/json`) {
                              complete( pretty(render(parse(write(sim)))))
                            }
                          }
                        }
                      }
                    } catch {
                      case ex : Exception => respondWithStatus(StatusCodes.NotAcceptable){ complete("Could not parse simulation metadata document") }
                    }
                  }
                }
              }
            } ~
            pathPrefix("run" / Segment ) { simid =>
              pathEndOrSingleSlash {
                get {
                  respondWithMediaType(`application/json`) {
                    DataStoreAccess.ds.getRunDoc(simid) match {
                      case Some(rundoc) => complete(rundoc)
                      case None => respondWithStatus(StatusCodes.NotFound) {
                        complete(s"Could not find and run instances for ${simid}")
                      }
                    }
                  }
                } ~
                post {
                  entity(as[String]) {
                    noopDoc =>
                      respondWithMediaType(`application/json`) {
                        val runid = DataStoreAccess.ds.nextRunId(simid)
                        DataStoreAccess.ds.putRun(simid, runid) match {
                          case Some(s) =>
                            cloudRouter ! RunIt(simid, runid)
                            respondWithHeader(HttpHeaders.Location( hrefBuilder("run",simid,runid) )) {
                              respondWithStatus(StatusCodes.Accepted) {
                                complete( s""" {"href":${hrefBuilder("run",simid,runid)}"}""" )
                              }
                            }
                          case None => respondWithStatus(StatusCodes.BadRequest) {
                            complete("Could not add ${simid} to the runlist")
                          }
                        }
                      }
                  }
                }
              } ~
              path( IntNumber ~ Slash.?) { runid =>
                get {
                  respondWithMediaType(`application/json`) {
                    DataStoreAccess.ds.getRun(simid,runid) match {
                      case Some(simrun) => complete(pretty(render(parse(write(simrun)))))
                      case None => respondWithStatus(StatusCodes.NotFound) {
                            complete( s""" {"msg":"NotFound status of sim : ${simid} instance: ${runid}"} """ )
                        }
                      }
                  }
                } ~
                post {
                  parameter('command) { command => {
                    command match {
                      case "shutdown" => {
                        cloudRouter ! StopIt(simid, runid)
                        respondWithMediaType(`application/json`) {
                          respondWithStatus(StatusCodes.Accepted) {
                            complete( s""" {"msg":"sent shutdown command to : ${simid} instance: ${runid} "}""")
                          }
                        }
                      }
                      case bc => {
                        respondWithMediaType(`application/json`) {
                          respondWithStatus(StatusCodes.BadRequest) {
                            complete( s""" {"msg":"bad command : ${bc} command to : ${simid} instance: ${runid} "}""")
                          }
                        }
                      }
                    }
                  }
                  }
                }
              }
            } ~
            path("usr"){
              get {
                respondWithMediaType(`application/json`) {
                  complete(""" {"msg":"get user information"}""")
                }
              }
            }
          }*/

}

