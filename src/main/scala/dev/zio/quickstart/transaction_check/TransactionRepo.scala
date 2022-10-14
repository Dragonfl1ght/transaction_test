package dev.zio.quickstart.transaction_check

import zio.*

trait TransactionRepo:
  def register(trans: Transaction): Task[String]

  def lookup(id: String): Task[Option[Transaction]]
  
  def transactions: Task[List[Transaction]]

object TransactionRepo:
  def register(trans: Transaction): ZIO[TransactionRepo, Throwable, String] =
    ZIO.serviceWithZIO[TransactionRepo](_.register(trans))

  def lookup(id: String): ZIO[TransactionRepo, Throwable, Option[Transaction]] =
    ZIO.serviceWithZIO[TransactionRepo](_.lookup(id))

  def users: ZIO[TransactionRepo, Throwable, List[Transaction]] =
    ZIO.serviceWithZIO[TransactionRepo](_.transactions)


