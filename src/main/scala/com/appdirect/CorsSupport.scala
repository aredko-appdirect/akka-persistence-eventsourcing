package com.appdirect

import com.typesafe.config.ConfigFactory
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directive0

trait CorsSupport {
  lazy val allowedOrigin = {
    HttpOrigin("http://localhost:9000")
  }

  //this directive adds access control headers to normal responses
  private def addAccessControlHeaders: Directive0 = {
    mapResponseHeaders { headers =>
      `Access-Control-Allow-Origin`(allowedOrigin) +:
      `Access-Control-Allow-Credentials`(true) +:
      `Access-Control-Allow-Headers`("Authorization", "Content-Type", "X-Requested-With") +:
       headers
    }
  }

  //this handles preflight OPTIONS requests. TODO: see if can be done with rejection handler,
  //otherwise has to be under addAccessControlHeaders
  private def preflightRequestHandler: Route = options {
    complete(HttpResponse(200).withHeaders(
      `Access-Control-Allow-Methods`(OPTIONS, POST, PUT, GET, DELETE)
    )
    )
  }

  def corsHandler(r: Route) = addAccessControlHeaders {
    preflightRequestHandler ~ r
  }
}