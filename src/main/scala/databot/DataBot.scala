package databot 

// created from https://github.com/patriknw/akka-data-replication/blob/master/src/test/scala/akka/contrib/datareplication/sample/DataBot.scala

import scala.concurrent.duration._
import akka.actor.ActorLogging
import akka.contrib.datareplication.protobuf.msg.ReplicatorMessages.GetSuccess
import akka.contrib.datareplication.DataReplication
import akka.cluster.Cluster
import akka.contrib.datareplication.Replicator
import akka.actor.Actor
import akka.contrib.datareplication.ORSet
import scala.concurrent.forkjoin.ThreadLocalRandom
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import akka.actor.Props
import Replicator._

object DataBot {

  def main(args: Array[String]): Unit = {

    val system = startup(args(0))
   
    val bot = system.actorOf(Props[DataBot], name = "dataBot")
  
    println("ready")
    while(true){
     bot ! Console.readLine
    }
  }

  def startup(port: String) = {

    // Override the configuration of the port
    val config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).
      withFallback(ConfigFactory.load(
        ConfigFactory.parseString("""
            akka.actor.provider = "akka.cluster.ClusterActorRefProvider"
            akka.remote {
              netty.tcp {
                hostname = "localhost"
                port = 0
              }
            }
            
            akka.cluster {
              seed-nodes = [
                "akka.tcp://ClusterSystem@localhost:2551"
               ]
            
            //  auto-down-unreachable-after = 10s
            }
            """)))

    // Create an Akka system
    ActorSystem("ClusterSystem", config)
  }

}

// This sample is used in the README.md (remember to copy when it is changed)
class DataBot extends Actor with ActorLogging {
  import DataBot._
  import Replicator._
  val replicator = DataReplication(context.system).replicator
  implicit val cluster = Cluster(context.system)
 
  replicator ! Subscribe("key", self)

  def receive = {
   
    case s:String => replicator ! Update("key", ORSet(),Some("request"))(_ + s)

    case ur: UpdateResponse => //log.info("update: {}", ur) 

    case Changed("key", data: ORSet) =>  println(data.value)
    //  log.info("Current elements: {}", data.value)
  }
}

