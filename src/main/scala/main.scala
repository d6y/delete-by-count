import slick.driver.H2Driver.api._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object Example extends App {

  final case class Message(content: String)

  final class MessageTable(tag: Tag) extends Table[Message](tag, "message") {
    def content = column[String]("content")
    def * = content <> (Message.apply, Message.unapply)
  }

  lazy val messages = TableQuery[MessageTable]

  // Delete all messages that have a duplicate (leaving zero rows with that content)
  val zap =
    messages.filter { msg =>
      messages.filter(_.content === msg.content).size > 1
    }.delete

  val program =
      messages.schema.create         >>
      (messages += Message("Hello")) >>
      (messages += Message("Hello")) >>
      (messages += Message("yo!"))   >>
      zap

  val db = Database.forConfig("db")
  try println(Await.result(db.run(program), 2 seconds)) finally db.close
}