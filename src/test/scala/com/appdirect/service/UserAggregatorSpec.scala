package com.appdirect.service

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.testkit.TestActorRef
import akka.util.Timeout
import com.appdirect.domain.user.UserAggregate._
import org.scalatest.{BeforeAndAfterAll, FlatSpec}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

class UserAggregatorSpec extends FlatSpec with BeforeAndAfterAll {

  import UserAggregator._

  implicit val actorSystem = ActorSystem("vehicleAggregateManagerSpec-system")

  implicit val timeout = Timeout(2 seconds)

  implicit val executionContext = actorSystem.dispatcher

  override def afterAll() = {
    actorSystem.shutdown()
  }

  "UserAggregateManager" should "create new child actor when creating new user" in {
    val manager = TestActorRef(UserAggregator.props, "UserAggregateManager-test-actor")

    val initialSize = manager.children.size

    manager ! RegisterUser(firstName = "fand", lastName = "rahimi", role = "test")

    val finalSize = manager.children.size

    assert(finalSize == initialSize + 1)
  }

  it should "use existing child actor when updating user data" in {
    val manager = TestActorRef(UserAggregator.props)

    //create a new vehicle
    val future = (manager ? RegisterUser(firstName = "fand", lastName = "rahimi", role = "test")).mapTo[User]

    val User(id, _, _, _) = Await.result(future, 2 seconds)

    val initialSize = manager.children.size

    //update the vehicle
    manager ! UpdateRole(id, "col2")

    //check children size
    val finalSize = manager.children.size

    assert(finalSize == initialSize)
  }

  it should "kill child actors when max count is exceeded" in {
    val manager = TestActorRef(UserAggregator.props)

    //create more vehicles than manager should keep
    implicit val timeout = Timeout(5 seconds)
    val futures = (0 to Aggregator.maxChildren * 2).foldLeft(Seq[Future[User]]()) { (futures, _) =>
      futures :+ (manager ? RegisterUser(firstName = "fand", lastName = "rahimi", role = "test")).mapTo[User]
    }

    val future = Future sequence futures
    Await.result(future, 5 seconds)

    val finalSize = manager.children.size

    assert(finalSize <= Aggregator.maxChildren)
  }

}
