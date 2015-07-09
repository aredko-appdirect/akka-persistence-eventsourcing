package com.appdirect.domain.user

import java.util.UUID

import akka.actor.{ActorLogging, ActorRef, Props}
import akka.pattern.{ask, pipe}
import akka.persistence.{PersistentActor, RecoveryCompleted, SnapshotOffer}
import akka.util.Timeout
import com.appdirect.domain.user.UserAggregate.UserSignUp

import scala.concurrent.duration._
import scala.util.Success

case class Register(email: String) extends Command

case class Registered(id: String, email: String, token: String) extends Event

case class Unregister(id: String) extends Command

case class Unregistered(id: String) extends Event

case class Users(users: List[String]) extends State[Users] {
  def updateState(event: Event) = event match {
    case Registered(id, _, _) => Users(id :: users)
    case Unregistered(id) => Users(users.filter(_ != id))
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
    case Unregister(id) => {
      persist(Unregistered(id)) { event =>
        updateState(event)
        context.child(id) foreach (context.stop(_))
      }
    }

    case Register(email) => {
      val id = UUID.randomUUID().toString()
      val token = UUID.randomUUID().toString()

      persist(Registered(id, email, token)) { event =>
        updateState(event)

        val child = context.child(s"user-$id") getOrElse context.actorOf(Props(new UserAggregate(id, storage)), s"user-$id")
        child ? new UserSignUp(email, UUID.randomUUID().toString()) pipeTo sender andThen {
          case Success(Error(_)) => self ! Unregister(id)
        }
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
