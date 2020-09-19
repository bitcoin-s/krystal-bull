package com.krystal.bull.storage

import org.bitcoins.crypto.{FieldElement, SchnorrNonce}
import org.bitcoins.db.{AppConfig, CRUD, DbCommonsColumnMappers, SlickUtil}
import slick.lifted.{ForeignKeyQuery, ProvenShape}

import scala.concurrent.{ExecutionContext, Future}

case class EventDAO()(implicit
    val ec: ExecutionContext,
    override val appConfig: AppConfig)
    extends CRUD[EventDb, SchnorrNonce]
    with SlickUtil[EventDb, SchnorrNonce] {

  import profile.api._

  private val mappers = new DbCommonsColumnMappers(profile)

  import mappers._

  implicit val fieldElementMapper: BaseColumnType[FieldElement] =
    MappedColumnType.base[FieldElement, String](_.hex, FieldElement.fromHex)

  override val table: TableQuery[EventTable] = TableQuery[EventTable]

  private lazy val rValueTable: profile.api.TableQuery[RValueDAO#RValueTable] =
    RValueDAO().table

  override def createAll(ts: Vector[EventDb]): Future[Vector[EventDb]] =
    createAllNoAutoInc(ts, safeDatabase)

  override protected def findByPrimaryKeys(
      ids: Vector[SchnorrNonce]): Query[EventTable, EventDb, Seq] =
    table.filter(_.nonce.inSet(ids))

  override protected def findAll(
      ts: Vector[EventDb]): Query[EventTable, EventDb, Seq] =
    findByPrimaryKeys(ts.map(_.nonce))

  class EventTable(tag: Tag) extends Table[EventDb](tag, schemaName, "events") {

    def nonce: Rep[SchnorrNonce] = column("nonce", O.PrimaryKey)

    def label: Rep[String] = column("label")

    def numOutcomes: Rep[Long] = column("numOutcomes")

    def attestationOpt: Rep[Option[FieldElement]] = column("attestation")

    def * : ProvenShape[EventDb] =
      (nonce,
       label,
       numOutcomes,
       attestationOpt) <> (EventDb.tupled, EventDb.unapply)

    def fk: ForeignKeyQuery[_, RValueDb] = {
      foreignKey("fk_nonce",
                 sourceColumns = nonce,
                 targetTableQuery = rValueTable)(_.nonce)
    }
  }
}
