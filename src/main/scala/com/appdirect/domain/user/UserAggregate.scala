package com.appdirect.domain.user

import akka.actor._
import akka.persistence._
import java.util.UUID
import com.appdirect.service.Create

trait Event
trait Command

trait State[T] {
  def updateState(event: Event): State[T]
}

case class Acknowledged(id: String)

object UserAggregate {
  case class User(id: String, firstName: String = "", lastName: String = "", email: String = "", token: String = "") extends State[User] {
    override def updateState(event: Event): State[User] = event match {
      case UserSignedUp(id, email, token) => copy(email = email, token = token)
    }
  }

  case class UserSignUp(email: String, token: String) extends Command
  case class UserSignedUp(id: String, email: String, token: String) extends Event
}

class UserAggregate(id: String, storage: ActorRef) extends PersistentActor with ActorLogging {
  import UserAggregate._

  override def persistenceId = id
  
  var state: State[User] = User(id)
  def updateState(event: Event): Unit = {
    state = state.updateState(event)
  }

  val receiveCommand: Receive = {
    case UserSignUp(email, token) =>
      persist(UserSignedUp(id, email, token)) { event => 
        updateState(event)
        storage ! Create(state.asInstanceOf[User])   
       
        context.system.eventStream.publish(event)
        sender ! Acknowledged(persistenceId)
      }
  }  
  
  val receiveRecover: Receive = {
    case event: Event => updateState(event)
    case SnapshotOffer(_, snapshot: User) => state = snapshot
  }
}
