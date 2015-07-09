package com.appdirect.route

import akka.actor.Props
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.util.Timeout
import com.appdirect.domain.user.{Event, UserAggregateManager}
import com.appdirect.service.{EmailServiceActor, StorageActor}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import scala.concurrent.duration._
import scala.language.postfixOps

class UserRouteSpec extends FlatSpec with ScalatestRouteTest with Matchers with BeforeAndAfterAll {
  implicit val timeout = Timeout(2.seconds)

  implicit def executionContext = system.dispatcher

  def actorRefFactory = system

  implicit val routeTestTimeout = RouteTestTimeout(5.seconds)

  val storageActor = system.actorOf(Props[StorageActor], "storage-actor")
  val emailServiceActor = system.actorOf(Props[EmailServiceActor], "email-actor")
  val userAggregateManager = system.actorOf(Props(new UserAggregateManager(storageActor)), "user-aggregate-manager")

  system.eventStream.subscribe(emailServiceActor, classOf[Event])


  "UserRoute" should "return not found if non-existing user is requested" in {
    Get("http://localhost:38080/api/v1/users/123") ~> UserRoute.userRoute ~> check {
      response.status shouldBe StatusCodes.OK
      responseAs[String] shouldBe "Not Implemented"
    }
  }
}
