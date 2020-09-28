package com.krystal.bull.core

import java.nio.file.{Files, Path}
import java.sql.SQLException

import org.bitcoins.crypto.{AesPassword, CryptoUtil, SchnorrDigitalSignature}
import org.bitcoins.testkit.fixtures.BitcoinSFixture
import org.bitcoins.testkit.util.FileUtil
import org.scalatest.FutureOutcome
import scodec.bits.ByteVector

import scala.concurrent.Future

class KrystalBullTest extends BitcoinSFixture {

  override type FixtureParam = KrystalBull

  def tmpDir(): Path = Files.createTempDirectory("krystal-bull-")

  override def withFixture(test: OneArgAsyncTest): FutureOutcome = {
    val builder: () => Future[KrystalBull] = () => {
      val conf = KrystalBullAppConfig(tmpDir())
      conf.initialize(AesPassword.fromString("bad"), None)
    }

    val destroy: KrystalBull => Future[Unit] = krystalBull => {
      val conf = krystalBull.conf
      conf.dropAll().flatMap { _ =>
        FileUtil.deleteTmpDir(conf.baseDatadir)
        conf.stop()
      }
    }
    makeDependentFixture(builder, destroy = destroy)(test)
  }

  val testOutcomes: Vector[String] = (0 to 10).map(_.toString).toVector

  behavior of "KrystalBull"

  it must "correctly initialize" in { krystalBull: KrystalBull =>
    assert(krystalBull.conf.seedExists())
  }

  it must "start with no events" in { krystalBull: KrystalBull =>
    krystalBull.listEventDbs().map { events =>
      assert(events.isEmpty)
    }
  }

  it must "start with no pending events" in { krystalBull: KrystalBull =>
    krystalBull.listPendingEventDbs().map { events =>
      assert(events.isEmpty)
    }
  }

  it must "create a new event and list it with pending" in {
    krystalBull: KrystalBull =>
      for {
        testEventDb <- krystalBull.createNewEvent("test", testOutcomes)
        pendingEvents <- krystalBull.listPendingEventDbs()
      } yield {
        assert(pendingEvents.size == 1)
        assert(pendingEvents.contains(testEventDb))
      }
  }

  it must "create a new event with a valid commitment signature" in {
    krystalBull: KrystalBull =>
      for {
        testEventDb <- krystalBull.createNewEvent("test", testOutcomes)
        rValDbOpt <- krystalBull.rValueDAO.read(testEventDb.nonce)
      } yield {
        assert(rValDbOpt.isDefined)
        val rValDb = rValDbOpt.get
        val hash = CryptoUtil.sha256(
          rValDb.nonce.bytes ++ ByteVector(rValDb.label.getBytes))
        assert(
          krystalBull.publicKey.verify(hash.bytes, rValDb.commitmentSignature))
      }
  }

  it must "create multiple events with different names" in {
    krystalBull: KrystalBull =>
      for {
        _ <- krystalBull.createNewEvent("test", testOutcomes)
        _ <- krystalBull.createNewEvent("test1", testOutcomes)
      } yield succeed
  }

  it must "fail to create multiple events with the same name" in {
    krystalBull: KrystalBull =>
      recoverToSucceededIf[SQLException] {
        for {
          _ <- krystalBull.createNewEvent("test", testOutcomes)
          _ <- krystalBull.createNewEvent("test", testOutcomes)
        } yield ()
      }
  }

  it must "create and sign a event" in { krystalBull: KrystalBull =>
    val outcome = testOutcomes.head
    for {
      eventDb <- krystalBull.createNewEvent("test", testOutcomes)
      sig <- krystalBull.signEvent(eventDb.nonce, outcome)
      outcomeDbs <- krystalBull.eventOutcomeDAO.findByNonce(eventDb.nonce)
      outcomeDb = outcomeDbs.find(_.message == outcome).get
      signedEvent <- krystalBull.eventDAO.read(eventDb.nonce)
    } yield {
      assert(signedEvent.isDefined)
      assert(signedEvent.get.attestationOpt.contains(sig.sig))
      assert(krystalBull.publicKey.verify(outcomeDb.hashedMessage.bytes, sig))
      assert(
        SchnorrDigitalSignature(signedEvent.get.nonce,
                                signedEvent.get.attestationOpt.get) == sig)
    }
  }
}
