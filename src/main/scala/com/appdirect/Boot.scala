package com.appdirect

import akka.pattern.ask
import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import akka.http.scaladsl.Http
import akka.actor.ActorSystem
import akka.event.{LoggingAdapter, Logging}
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import java.io.IOException
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.math._
import spray.json.DefaultJsonProtocol
import akka.http.scaladsl.server.RouteResult
import java.util.UUID
import com.appdirect.domain.user.UserAggregate
import com.appdirect.domain.user.UserAggregate.UserSignUp
import com.appdirect.domain.user.Event
import com.appdirect.service.EmailServiceActor
import scala.concurrent.duration._
import akka.util.Timeout
import com.appdirect.service.StorageActor
import com.appdirect.domain.user.UserAggregateManager
import com.appdirect.domain.user.Register
import com.appdirect.domain.user.Acknowledged
import com.appdirect.domain.user.Error

object Boot extends App with DefaultJsonProtocol {
  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()
  implicit val timeout: Timeout = 5 seconds

  val config = ConfigFactory.load()
  val logger = Logging(system, getClass)
  
  val storageActor = system.actorOf(Props[StorageActor], "storage-actor")
  val emailServiceActor = system.actorOf(Props[EmailServiceActor], "email-actor")
  val userAggregateManager = system.actorOf(Props(new UserAggregateManager(storageActor)), "user-aggregate-manager")
  system.eventStream.subscribe(emailServiceActor, classOf[Event])
  
  val routes = {
    logRequestResult("akka-eventsourced") {
      pathPrefix("api" / "v1") {
        path("users" / "signup") {
           (post & formFields('email.as[String])) { email =>
              complete {
                userAggregateManager ? Register(email) map { 
                  case Acknowledged(_) => "Signup successful: " + email
                  case Error(_) => "Email already exists: " + email
                }
              }                             
          } 
        }
        
//        path("users" / "activation" / Segment) { token =>
//           post {
//              complete {
//                val actor = system.actorSelection(, s"user-$id")
//              }                             
//          } 
//        }
      }
    }
  }
  
  Http().bindAndHandle(routes, "localhost", 38080)
}
