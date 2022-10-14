package dev.zio.quickstart.transaction_check

import zio.*

import scala.collection.mutable

case class InmemoryTransRepo(map: Ref[mutable.Map[String, Transaction]]) extends TransactionRepo:
  def register(trans: Transaction): UIO[String] =
    for
      id <- Random.nextUUID.map(_.toString)
      _ <- map.updateAndGet(_ addOne(id, trans))
    yield id

  def lookup(id: String): UIO[Option[Transaction]] =
    map.get.map(_.get(id))

  def users: UIO[List[Transaction]] =
     map.get.map(_.values.toList)

  object InmemoryTransRepo {
    def layer: ZLayer[Any, Nothing, InmemoryTransRepo] =
      ZLayer.fromZIO(
        Ref.make(mutable.Map.empty[String, Transaction]).map(new InmemoryTransRepo(_))
      )
  }