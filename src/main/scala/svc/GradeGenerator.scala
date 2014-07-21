package svc

//package persistance

import akka.actor._
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

/**
 * Created by Salah on 6/23/2014.
 */
class GradeGenerator extends Actor with akka.actor.ActorLogging{
  val cluster = Cluster(context.system)
  // subscribe to cluster changes, MemberUp
  // re-subscribe when restart
  override def preStart(): Unit = {
    log.info("*****************Starting a producer node*****************")
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember], classOf[MemberRemoved])
  }
  override def postStop(): Unit = cluster.unsubscribe(self)

  var act :ActorSelection = null
  //var nodes = Set.empty[Address]
  //var  nodes : String= ""
  val nodes = collection.mutable.Map[String, String]()
  def receive = {
    case va: Int =>
        act ! Grade (va, 3)
    case "Request" =>
    {
      implicit val timeout = Timeout(5 seconds)
      val future: Future[Any] = act.ask("Request")(5 seconds) // enabled by the “ask” import
      val result = Await.result(future, timeout.duration).asInstanceOf[Double]
      sender()! result
    }
    case "GetNodes" =>
      var temp = ""
      nodes.foreach {keyVal => temp += keyVal._1 + "=" + keyVal._2 + "          "}
      sender() ! temp
    case MemberUp(m) =>
      if (m.hasRole("consumer") | m.hasRole("producer")) {
        //nodes += "  Node with role" + m.getRoles + m.address
        nodes +=  m.address.toString ->  m.getRoles.toString

        if(m.hasRole("consumer"))
        {
          println("NEW ))))))))))))))))))))))))))))))))))))))))))))))")
          act = context.actorSelection(RootActorPath(m.address) / "user" / "consumer")
        }

      }
      /*if (m.hasRole("consumer")) {

        val act = context.actorSelection(RootActorPath(m.address) / "user" / "consumer")
        var inputGrd = ""
        do {
          println("Plese enter a grade of Exit")
          inputGrd = Console.readLine()
          if (inputGrd == "Throw")
          {
            act ! "Throw"
            Thread.sleep(500)
          }
          else if(inputGrd == "Request")
          {
            implicit val timeout = Timeout(5 seconds)
            val future: Future[Any] = act.ask("Request")(5 seconds) // enabled by the “ask” import
          val result = Await.result(future, timeout.duration).asInstanceOf[Double]
            println("Current Persistance.GPA:" + result)
          }
          else if (inputGrd != "Exit" && inputGrd !="") {
            implicit val timeout = Timeout(5 seconds)
            act ! Grade(inputGrd.toInt, 3) // enabled by the “ask” import
            val future: Future[Any] = act.ask("Request")(5 seconds) // enabled by the “ask” import
            val result = Await.result(future, timeout.duration).asInstanceOf[Double]
            println("Current Persistance.GPA:" + result)
          }
        } while (inputGrd != "Exit")
      }*/
    case UnreachableMember(m) =>
    {
      if (m.hasRole("consumer") | m.hasRole("producer")) {
        nodes -=  m.address.toString
        //println("Whhhhhhhhhhhhhhhhhhhhhhhhhhhhhat:" + m.address)
      }
    }
    case MemberRemoved(m, previousStatus) =>
      if (m.hasRole("consumer"))
      {
        //val act = context.actorSelection(RootActorPath(m.address) / "user" / "consumer")
        //Cluster(context.system).down(m.address)
        println()
        println("**********************RESTARTING CONSUMER NODE ON DIFFERENT NODE ************************")
        println()
        val port =2558;
        val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
          withFallback(ConfigFactory.parseString("akka.cluster.roles = [consumer]")).
          withFallback(ConfigFactory.parseString(
          """
            |akka.persistence.journal.plugin = "cassandra-journal"
            |akka.persistence.snapshot-store.plugin = "cassandra-snapshot-store"
          """.stripMargin)).
          withFallback(ConfigFactory.load())
        val system = ActorSystem("ClusterSystem", config)
        val act = system.actorOf(Props[GPAInterface], name = "consumer")
      }


  }
}

object GradeGenerator {
  def main(args: Array[String]): Unit = {
    val port = if (args.isEmpty) "0" else args(0)
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
      withFallback(ConfigFactory.parseString("akka.cluster.roles = [producer]")).
      withFallback(ConfigFactory.load())


    val system = ActorSystem("ClusterSystem", config)
    val producer = system.actorOf(Props[GradeGenerator], name = "producer")
    ////////////
    val xx = 22
    println(xx)
  }
}