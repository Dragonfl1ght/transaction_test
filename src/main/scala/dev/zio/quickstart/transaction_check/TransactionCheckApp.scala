package dev.zio.quickstart.transaction_check

import zhttp.http.*
import zio.*
import zio.json.*

object TransactionCheckApp:
  def apply(): Http[TransactionRepo, Throwable, Request, Response] =
  Http.collectZIO[Request] {
    // POST /users -d '{"name": "John", "age": 35}'
    case req@(Method.POST -> !! / "trsansaction_check") =>
      for
        u <- req.bodyAsString.map(_.fromJson[Transaction])
        r <- u match
          case Left(e) =>
            ZIO.debug(s"Failed to parse the input: $e").as(
              Response.text(e).setStatus(Status.BadRequest)
            )
          case Right(u) =>
            TransactionRepo.register(u)
              .map(id => Response.text(id))
      yield r

    // GET /users/:id
    case Method.GET -> !! / "trsansaction_check" / id =>
      TransactionRepo.lookup(id)
        .map {
          case Some(trans) =>
            Response.json(trans.toJson)
          case None =>
            Response.status(Status.NotFound)
        }
    // GET /users
    case Method.GET -> !! / "trsansaction_check" =>
      TransactionRepo.trans.map(response => Response.json(response.toJson))
  }
