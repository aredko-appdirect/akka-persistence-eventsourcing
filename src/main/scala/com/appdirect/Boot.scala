package com.appdirect

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import com.appdirect.ApiActor
import spray.can._

//https://github.com/NET-A-PORTER/spray-actor-per-request/blob/master/src/main/scala/com/netaporter/Boot.scala
object Boot extends App {

  implicit val system = ActorSystem("user-management")
  
  implicit val executionContext = system.dispatcher

  val serviceActor = system.actorOf(Props(new ApiActor), "actor-service")

  IO(Http) ! Http.Bind(serviceActor, interface = "localhost", port = 38080)
}
