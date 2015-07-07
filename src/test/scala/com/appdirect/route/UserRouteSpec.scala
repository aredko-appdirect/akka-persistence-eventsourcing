package com.appdirect.route

import java.util.UUID

import akka.pattern.ask
import akka.util.Timeout
import com.appdirect.domain.AggregateRoot
import com.appdirect.domain.user.UserAggregate
import com.appdirect.route.user.UserRoute
import com.appdirect.service.UserAggregator
import AggregateRoot.Removed
import UserAggregate.User
import UserAggregator.{GetUser, RegisterUser}
import org.json4s.{DefaultFormats, JObject}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import spray.http.StatusCodes
import spray.testkit.ScalatestRouteTest

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class UserRouteSpec extends FlatSpec with ScalatestRouteTest with Matchers with UserRoute with BeforeAndAfterAll {

  implicit val json4sFormats = DefaultFormats

  implicit val timeout = Timeout(2.seconds)

  implicit def executionContext = system.dispatcher

  def actorRefFactory = system

  val userAggregator = system.actorOf(UserAggregator.props)

  implicit val routeTestTimeout = RouteTestTimeout(5.seconds)

  "UserRoute" should "return not found if non-existing user is requested" in {
    Get("/users/" + UUID.randomUUID().toString) ~> userRoute ~> check {
      response.status shouldBe StatusCodes.NotFound
    }
  }

  it should "create a user" in {
    val firstName = "esfand"
    val lastName = "bla"
    val role = "test"
    Post("/users", Map("firstName" -> firstName,"lastName" -> lastName, "role" -> role)) ~> userRoute ~> check {
      response.status shouldBe StatusCodes.Created
      val id = (responseAs[JObject] \ "id").extract[String]
      val user = getUserFromManager(id)
      user.firstName shouldEqual firstName
      user.lastName shouldEqual lastName
      user.role shouldEqual role
    }
  }

  it should "return existing user" in {
    val firstName = "esfand"
    val lastName = "bla"
    val role = "test"
    val user = createUserInManager(firstName, lastName, role)
    Get(s"/users/" + user.id) ~> userRoute ~> check {
      response.status shouldBe StatusCodes.OK
      val responseJson = responseAs[JObject]
      (responseJson \ "firstName").extract[String] shouldEqual firstName
      (responseJson \ "lastName").extract[String] shouldEqual lastName
      (responseJson \ "role").extract[String] shouldEqual role
    }
  }

  it should "remove user" in {
    val user = createUserInManager("esfandiar", "another", "test")
    Delete("/users/" + user.id) ~> userRoute ~> check {
      response.status shouldBe StatusCodes.NoContent
      val emptyUserFuture = userAggregator ? GetUser(user.id)
      val emptyUser = Await.result(emptyUserFuture, 2.seconds)
      emptyUser shouldBe Removed
    }
  }

  it should "update user's name" in {
    val user = createUserInManager("esfandiar", "Persian", "test")
    val firstName = "esfand"
    val lastName = "bla"
    Post(s"/users/${user.id}/name", Map("firstName" -> firstName, "lastName" -> lastName)) ~> userRoute ~> check {
      response.status shouldBe StatusCodes.OK
      val updatedUser = getUserFromManager(user.id)
      updatedUser.firstName shouldEqual firstName
      updatedUser.lastName shouldEqual lastName
    }
  }

  it should "update user's role" in {
    val user = createUserInManager("john", "Doe", "test")
    val newRole = "god"
    Post(s"/users/${user.id}/role", Map("value" -> newRole)) ~> userRoute ~> check {
      response.status shouldBe StatusCodes.OK
      val updatedUser = getUserFromManager(user.id)
      updatedUser.role shouldEqual newRole
    }
  }

  private def getUserFromManager(id: String) = {
    val userFuture = (userAggregator ? GetUser(id)).mapTo[User]
    Await.result(userFuture, 2.seconds)
  }

  private def createUserInManager(firstName: String,lastName: String, role: String) = {
    val userFuture = (userAggregator ? RegisterUser(firstName, lastName, role)).mapTo[User]
    Await.result(userFuture, 2.seconds)
  }

}
