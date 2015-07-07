package com.appdirect.service

import java.util.UUID

import akka.actor._
import com.appdirect.domain.AggregateRoot
import com.appdirect.domain.user.UserAggregate

object UserAggregator {

  import Aggregator._

  case class RegisterUser(firstName: String, lastName: String, role: String) extends Command

  case class GetUser(id: String) extends Command

  case class UpdateName(id: String, firstName: String, lastName: String) extends Command

  case class UpdateRole(id: String, role: String) extends Command

  case class DeleteUser(id: String) extends Command

  def props: Props = Props(new UserAggregator)
}

class UserAggregator extends Aggregator {

  import AggregateRoot._
  import UserAggregate._
  import UserAggregator._


  def processCommand = {
    case RegisterUser(firstName, lastName, role) =>
      val id = UUID.randomUUID().toString
      processAggregateCommand(id, Initialize(firstName, lastName, role))
    case GetUser(id) =>
      processAggregateCommand(id, GetState)
    case UpdateName(id, firstName, lastName) =>
      processAggregateCommand(id, ChangeName(firstName, lastName))
    case UpdateRole(id, role) =>
      processAggregateCommand(id, ChangeRole(role))
    case DeleteUser(id) =>
      processAggregateCommand(id, Remove)
  }

  override def aggregateProps(id: String) = UserAggregate.props(id)
}
