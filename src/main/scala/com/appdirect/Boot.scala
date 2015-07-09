package com.appdirect

import akka.http.scaladsl.Http
import com.appdirect.route.UserRoute._
import spray.json.DefaultJsonProtocol


object Boot extends App with DefaultJsonProtocol {
  Http().bindAndHandle(userRoute, "localhost", 38080)
}
