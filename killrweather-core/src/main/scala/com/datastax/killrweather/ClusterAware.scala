package com.datastax.killrweather

import akka.actor.{Actor, ActorLogging}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._

class ClusterAware extends Actor with ActorLogging {

  val cluster = Cluster(context.system)

  /** subscribe to cluster changes, re-subscribe when restart. */
   override def preStart(): Unit =
     cluster.subscribe(self, classOf[ClusterDomainEvent])

  override def postStop(): Unit = cluster.unsubscribe(self)

  def receive : Actor.Receive = {
    case LeaderChanged(leader) =>
      log.info("Leader changed to {}", leader)
    case ClusterMetricsChanged(forNode) =>
      log.info("Cluster metrics update:")
    case MemberUp(member) =>
      log.info("Member is Up: {}", member.address)
    case UnreachableMember(member) =>
      log.info("Member detected as unreachable: {}", member)
    case MemberRemoved(member, previousStatus) =>
      log.info("Member is Removed: {} after {}",
        member.address, previousStatus)
    case _: MemberEvent => // ignore
  }
}