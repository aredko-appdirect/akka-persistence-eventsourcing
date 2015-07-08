package com.appdirect.domain.user

import akka.pattern.{ask, pipe}
import akka.actor._
import akka.persistence._
import java.util.UUID
import com.appdirect.service.Create
import scala.concurrent.duration._
import akka.util.Timeout
import scala.util.Failure
import scala.util.Try
import scala.util.Success

trait Event
trait Command

trait State[T] {
  def updateState(event: Event): State[T]
}

case class Acknowledged(id: String)
case class Error(id: String)

object UserAggregate {
  case class User(id: String, firstName: String = "", lastName: String = "", email: String = "", token: String = "") extends State[User] {
    override def updateState(event: Event): State[User] = event match {
      case UserSignedUp(id, email, token) => copy(email = email, token = token)
    }
  }

  case class UserSignUp(email: String, token: String) extends Command
  case class UserSignedUp(id: String, email: String, token: String) extends Event
  case class UserSignUpRefused(id: String) extends Event  
}

class UserAggregate(id: String, storage: ActorRef) extends PersistentActor with ActorLogging {
  import UserAggregate._
  import scala.concurrent.ExecutionContext.Implicits.global

  override def persistenceId = id
  
  implicit val timeout: Timeout = 5 seconds 
  var state: State[User] = User(id)
  
  def updateState(event: Event): Unit = {
    state = state.updateState(event)
  }
  
  val receiveCommand: Receive = {
    case UserSignUp(email, token) => {
      persist(UserSignedUp(id, email, token)) { event => 
        updateState(event)        
      
        val replyTo = sender
        storage ? Create(state) onComplete { 
            case Success(_) => {
              context.system.eventStream.publish(event)
              replyTo ! Acknowledged(persistenceId)
            }
            
            case Failure(ex) => {
              replyTo ! Error(persistenceId) 
            }
        }
      }
    }
  }  
  
  override def receiveRecover: Receive = {
    case event: Event => updateState(event)
    case SnapshotOffer(_, snapshot: User) => state = snapshot
  }
}
