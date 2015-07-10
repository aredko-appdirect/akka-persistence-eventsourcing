package com.appdirect.route

import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.appdirect.domain.user.{Event, UserAggregateManager, _}
import com.appdirect.service.{EmailServiceActor, StorageActor}
import scala.concurrent.duration._
import com.appdirect.CorsSupport
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.ContentTypes._

object UserRoute extends CorsSupport {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  implicit val executor = system.dispatcher
  implicit val timeout: Timeout = 5 seconds

  val logger = Logging(system, getClass)

  val storageActor = system.actorOf(Props[StorageActor], "storage-actor")
  val emailServiceActor = system.actorOf(Props[EmailServiceActor], "email-actor")
  val userAggregateManager = system.actorOf(Props(new UserAggregateManager(storageActor)), "user-aggregate-manager")

  system.eventStream.subscribe(emailServiceActor, classOf[Event])

  val userRoute = {
    logRequestResult("akka-eventsourced") {
      pathPrefix("api" / "v1" / "users") {
        corsHandler {
          path("signup") {
            (post & formFields('email.as[String])) { email =>
              complete {
                userAggregateManager ? Register(email) map {
                  case Acknowledged(_) => HttpResponse(status = OK, entity = "Signup successful: " + email)
                  case Error(_) => HttpResponse(status = Conflict, entity = "Email already exists: " + email)
                }
              }
            }
          }
        } ~
        corsHandler {
          path(Segment) { token =>
            get {
              complete {
                "Not Implemented"
              }
            }
          }
        }
      }
    }
  }
}
