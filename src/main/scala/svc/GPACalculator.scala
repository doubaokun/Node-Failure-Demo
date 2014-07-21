package svc

/**
 * Created by Salah on 6/23/2014.
 */
//package persistance

import akka.actor.SupervisorStrategy._
import akka.actor._
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.MemberUp
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}


class GPAInterface extends Actor {
  // Stop the CounterService child if it throws ServiceUnavailable
  val cluster = Cluster(context.system)

  // subscribe to cluster changes, MemberUp
  // re-subscribe when restart
  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])
  override def postStop(): Unit = cluster.unsubscribe(self)

  override val supervisorStrategy = OneForOneStrategy() {
    case _: Exception_Msgs.Exception_Msg =>Restart
  }
  //val test = context.actorOf(Props[GPA_Calulation_ES],"Test")
  val test = context.actorOf(Props(classOf[GPA_Calulation_ES], "Calculator"))

  def receive = {
    case MemberUp(m) => println("")
    case Grade(grd, hr)=>  test ! Grade(grd, hr)
    case "Request" =>
      implicit val timeout = Timeout(5 seconds)
      val future: Future[Any] = test.ask("Request")(5 seconds) // enabled by the “ask” import
      val result = Await.result(future, timeout.duration).asInstanceOf[GPA]
      sender() ! result.ctr
      println(result.ctr)
    case "Throw" => test ! "Throw"
    case _ =>
      println("Stop Working......")
  }
}

object GPACalculator {
  def main(args: Array[String]): Unit = {
    val port = if (args.isEmpty) "0" else args(0)
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
      withFallback(ConfigFactory.parseString("akka.cluster.roles = [consumer]")).
      withFallback(ConfigFactory.parseString(
      """
        |akka.persistence.journal.plugin = "cassandra-journal"
        |akka.persistence.snapshot-store.plugin = "cassandra-snapshot-store"
      """.stripMargin)).
      withFallback(ConfigFactory.load())
    val system = ActorSystem("ClusterSystem", config)
    val consumer = system.actorOf(Props[GPAInterface], name = "consumer")

    }

}