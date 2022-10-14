package dev.zio.quickstart.transaction_check

import io.getquill.context.ZioJdbc.DataSourceLayer
import io.getquill.{Escape, H2ZioJdbcContext}
import io.getquill.jdbczio.Quill
import io.getquill.*
import zio.*

import java.util.UUID
import javax.sql.DataSource

case class UserTable(uuid: UUID, src: Int, dst: Int, amount: Int)

case class PersistentTransRepo(ds: DataSource) extends TransactionRepo:
  val ctx = new H2ZioJdbcContext(Escape)

  import ctx._

  override def register(trans: Transaction): Task[String] = {
    for
      id <- Random.nextUUID
      _ <- ctx.run {
        quote {
          query[UserTable].insertValue {
            lift(UserTable(id, trans.src, trans.dst, trans.amount))
          }
        }
      }
    yield id.toString
  }.provide(ZLayer.succeed(ds))

  override def lookup(id: String): Task[Option[Transaction]] =
    ctx.run {
      quote {
        query[UserTable]
          .filter(p => p.uuid == lift(UUID.fromString(id)))
          .map(t => Transaction(t.src, t.dst, t.amount))
      }
    }.provide(ZLayer.succeed(ds)).map(_.headOption)

  override def transactions: Task[List[Transaction]] =
    ctx.run {
      quote {
        query[UserTable].map(t => Transaction(t.src, t.dst, t.amount))
      }
    }.provide(ZLayer.succeed(ds))

object PersistentTransRepo:
  def layer: ZLayer[Any, Throwable, PersistentTransRepo] =
    Quill.DataSource.fromPrefix("UserApp") >>>
      ZLayer.fromFunction(PersistentTransRepo(_))
