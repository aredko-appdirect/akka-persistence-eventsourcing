package com.appdirect.domain.user

import akka.actor._
import akka.persistence._
import com.appdirect.domain.AggregateRoot

object UserAggregate {

  import AggregateRoot._

  case class User(id: String, firstName: String = "", lastName: String = "", role: String = "") extends State

  case class Initialize(firstName: String, lastName: String, role: String) extends Command
  case class ChangeName(firstName: String, lastName: String) extends Command
  case class ChangeRole(role: String) extends Command

  case class UserInitialized(firstName: String, lastName: String, role: String) extends Event
  case class NameChanged(firstName: String, lastName: String) extends Event
  case class RoleChanged(role: String) extends Event
  case object UserRemoved extends Event

  def props(id: String): Props = Props(new UserAggregate(id))
}

class UserAggregate(id: String) extends AggregateRoot {

  import AggregateRoot._
  import UserAggregate._

  override def persistenceId = id

  override def updateState(evt: AggregateRoot.Event): Unit = evt match {
    case UserInitialized(firstName, lastName, role) =>
      context.become(created)
      state = User(id, firstName, lastName, role)
    case NameChanged(fName, lName) => state match {
      case s: User => state = s.copy(firstName = fName, lastName = lName)
      case _ => //nothing
    }
    case RoleChanged(rl) => state match {
      case s: User => state = s.copy(role = rl)
      case _ => //nothing
    }
    case UserRemoved =>
      context.become(removed)
      state = Removed
  }

  val initial: Receive = {
    case Initialize(firstName, lastName, role) =>
      persist(UserInitialized(firstName, lastName, role))(afterEventPersisted)
    case GetState =>
      respond()
    case KillAggregate =>
      context.stop(self)
  }
  
  val created: Receive = {
    case ChangeName(firstName, lastName) =>
      persist(NameChanged(firstName, lastName))(afterEventPersisted)
    case ChangeRole(role) =>
      persist(RoleChanged(role))(afterEventPersisted)
    case Remove =>
      persist(UserRemoved)(afterEventPersisted)
    case GetState =>
      respond()
    case KillAggregate =>
      context.stop(self)
  }
  
  val removed: Receive = {
    case GetState =>
      respond()
    case KillAggregate =>
      context.stop(self)
  }

  val receiveCommand: Receive = initial

  override def restoreFromSnapshot(metadata: SnapshotMetadata, state: State) = {
    this.state = state
    state match {
      case Uninitialized => context become initial
      case Removed => context become removed
      case _: User => context become created
    }
  }

}
