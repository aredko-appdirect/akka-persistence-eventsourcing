package com.appdirect

import akka.actor._
import com.appdirect.route.user.UserRoute
import com.appdirect.service.UserAggregator
import org.json4s.DefaultFormats
import spray.http.MediaTypes._

class ApiActor extends Actor with ActorLogging with UserRoute {

  val json4sFormats = DefaultFormats

  implicit def actorRefFactory = context

  implicit val executionContext = context.dispatcher
  
  val userAggregator = context.actorOf(UserAggregator.props)

  def receive =
    runRoute(
      pathPrefix("api/v1") {
        respondWithMediaType(`application/json`) {
          userRoute
        }
      }
    )

}
