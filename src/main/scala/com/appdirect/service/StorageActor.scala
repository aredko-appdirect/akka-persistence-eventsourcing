package com.appdirect.service

import akka.actor.{Actor, ActorLogging}
import com.appdirect.domain.user.State
import com.appdirect.domain.user.UserAggregate.User
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONDocument
import akka.pattern.pipe


case class Create(state: State[_])

case class Update(state: State[_])

case class Delete(state: State[_])

case class Find(token: String)


class StorageActor extends Actor with ActorLogging {

  import reactivemongo.api._

  import scala.concurrent.ExecutionContext.Implicits.global

  // gets an instance of the driver
  // (creates an actor system)
  val driver = new MongoDriver
  val connection = driver.connection(List("localhost"))

  // Gets a reference to the database "plugin"
  val db = connection("eventsourced")

  def receive = {
    case Create(user: User) => {
      val collection = db.collection("users")
      collection.indexesManager.ensure(new Index(Seq(("email", IndexType.Ascending)), None, true))

      collection.insert(
        BSONDocument(
          "_id" -> user.id,
          "email" -> user.email,
          "token" -> user.token
        )
      ) pipeTo sender
    }
  }
}
