package com.appdirect.service

import akka.http.scaladsl.model.headers.ContentDispositionTypes.Ext
import akka.actor.ActorLogging
import com.appdirect.domain.user.UserAggregate.UserSignedUp
import akka.actor.Actor


class EmailServiceActor extends Actor with ActorLogging {
  def receive = {
    case UserSignedUp(id, email, token) => log.info(s"http://localhost:38080/api/v1/user/activate/$token")
  }
}