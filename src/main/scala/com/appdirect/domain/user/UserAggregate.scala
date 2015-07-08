package com.appdirect.domain.user

import akka.actor._
import akka.persistence._

trait Event
trait Command
trait State

object UserAggregate {

  case class User(id: String, firstName: String = "", lastName: String = "", email: String = "") extends State

  case class UserActivate(token: String) extends Command
  case class UserSignUp(firstName: String, lastName: String, email: String) extends Command

  case class UserSignedUp(firstName: String, lastName: String, email: String) extends Event
  case class UserActivates(id: String) extends Event

  def props(id: String): Props = Props(new UserAggregate(id))
}

class UserAggregate(id: String) extends PersistentActor with ActorLogging {

  import UserAggregate._

  var state: State = ???
  def persistenceId = id
  def updateState(event: Event): Unit = ??? 
  

  val receiveCommand: Receive = {
    case UserSignUp(firstName, lastName, email) =>
      persist(UserSignedUp(firstName, lastName, email))(updateState)
  }  
  
  val receiveRecover: Receive = {
    case event: Event => updateState(event)
    case SnapshotOffer(_, snapshot: User) => state = snapshot
  }
}
