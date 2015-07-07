package com.appdirect.route.user

import akka.actor._
import com.appdirect.domain.user.UserAggregate
import com.appdirect.route.RequestHandlerCreator
import com.appdirect.service.{Aggregator, UserAggregator}
import spray.httpx.Json4sSupport
import spray.routing._

object UserRoute {
  case class UpdateUserData(value: String)
  case class UpdateUserData2(firstName: String, lastName: String)
}

trait UserRoute extends HttpService with Json4sSupport with RequestHandlerCreator {

  import UserAggregator._
  import UserRoute._

  val userAggregator: ActorRef

  val userRoute =
    path("users" / Segment / "name" ) { id =>
      post {
          entity(as[UpdateUserData2]) { cmd =>
            serveUpdate(UpdateName(id, cmd.firstName, cmd.lastName))
          }
      }
    } ~
    path("users" / Segment / "role" ) { id =>
      post {
          entity(as[UpdateUserData]) { cmd =>
            serveUpdate(UpdateRole(id, cmd.value))
          }
      }
    } ~
    path("users" / Segment ) { id =>
      get {
        serveGet(GetUser(id))
      } ~
      delete {
          serveDelete(DeleteUser(id))
      }
    } ~
    path("users") {
        post {
          entity(as[RegisterUser]) { cmd =>
            serveRegister(cmd)
          }
        }
    }

  private def serveUpdate(message : Aggregator.Command): Route =
    ctx => handleUpdate[UserAggregate.User](ctx, userAggregator, message)

  private def serveRegister(message : Aggregator.Command): Route =
    ctx => handleRegister[UserAggregate.User](ctx, userAggregator, message)

  private def serveDelete(message : Aggregator.Command): Route =
    ctx => handleDelete(ctx, userAggregator, message)

  private def serveGet(message : Aggregator.Command): Route =
    ctx => handleGet[UserAggregate.User](ctx, userAggregator, message)

}
