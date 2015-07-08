package com.appdirect.domain.user

import akka.pattern.{ask, pipe}
import akka.persistence.PersistentActor
import akka.actor.ActorLogging
import akka.persistence.SnapshotOffer
import com.appdirect.domain.user.UserAggregate.UserSignedUp
import java.util.UUID
import akka.actor.Props
import akka.actor.ActorRef
import com.appdirect.domain.user.UserAggregate.UserSignUp
import scala.concurrent.duration._
import akka.util.Timeout
import akka.persistence.RecoveryCompleted


case class Register(email: String) extends Command
case class Registered(id: String, email: String, token: String) extends Event

case class Users(users: List[String]) extends State[Users] {
   def updateState(event: Event) = event match {
     case Registered(id, _, _) => Users(id :: users)
   }
}

class UserAggregateManager(storage: ActorRef) extends PersistentActor with ActorLogging {
  import scala.concurrent.ExecutionContext.Implicits.global
 
  override def persistenceId = "user-aggregate-manager"
  
  implicit val timeout: Timeout = 5 seconds
  var state: State[Users] = Users(List())
  
  def updateState(event: Event): Unit = {
    state = state.updateState(event) 
  }
  
  override val receiveCommand: Receive = {
    case Register(email) => {
      val id = UUID.randomUUID().toString()
      val token = UUID.randomUUID().toString()
      
      persist(Registered(id, email, token)) { event => 
        updateState(event)
        
        val child = context.child(s"user-$id") getOrElse context.actorOf(Props(new UserAggregate(id, storage)), s"user-$id")
        child ? new UserSignUp(email, UUID.randomUUID().toString()) pipeTo sender
      }
    }
  }
  
  override def receiveRecover: Receive = {
    case RecoveryCompleted => state.asInstanceOf[Users].users.foreach { id =>
      context.actorOf(Props(new UserAggregate(id, storage)), s"user-$id")
    }
    
    case event: Event => updateState(event)
    case SnapshotOffer(_, snapshot: Users) => state = snapshot
  }
}