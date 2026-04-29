// QuaiControllerSpec.scala : tests unitaires de l'arbitrage du quai.
// Symetrique de SectionControllerSpec mais pour le quai (ArriveeQuai / DepartQuai).

package m14.troncon

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import m14.troncon.Protocol._
import org.scalatest.wordspec.AnyWordSpecLike

class QuaiControllerSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "QuaiController" should {

    "donner Autorisation au premier ArriveeQuai (quai libre)" in {
      val quai = spawn(QuaiController())
      val probeT1 = createTestProbe[MessagePourTrain]()

      quai ! ArriveeQuai(Train1, probeT1.ref)
      probeT1.expectMessage(Autorisation)
    }

    "envoyer Attente au second train quand le quai est occupe" in {
      val quai = spawn(QuaiController())
      val probeT1 = createTestProbe[MessagePourTrain]()
      val probeT2 = createTestProbe[MessagePourTrain]()

      quai ! ArriveeQuai(Train1, probeT1.ref)
      probeT1.expectMessage(Autorisation)

      quai ! ArriveeQuai(Train2, probeT2.ref)
      probeT2.expectMessage(Attente)
    }

    "promouvoir le train en attente apres DepartQuai de l occupant" in {
      val quai = spawn(QuaiController())
      val probeT1 = createTestProbe[MessagePourTrain]()
      val probeT2 = createTestProbe[MessagePourTrain]()

      quai ! ArriveeQuai(Train1, probeT1.ref)
      probeT1.expectMessage(Autorisation)
      quai ! ArriveeQuai(Train2, probeT2.ref)
      probeT2.expectMessage(Attente)

      // T1 quitte le quai -> T2 doit etre promu (recoit Autorisation).
      quai ! DepartQuai(Train1)
      probeT2.expectMessage(Autorisation)
    }

    "ignorer DepartQuai d un train qui n est pas l occupant courant" in {
      val quai = spawn(QuaiController())
      val probeT1 = createTestProbe[MessagePourTrain]()
      val probeT2 = createTestProbe[MessagePourTrain]()

      quai ! ArriveeQuai(Train1, probeT1.ref)
      probeT1.expectMessage(Autorisation)

      // DepartQuai parasite de Train2 (pas occupant) : doit etre ignore.
      quai ! DepartQuai(Train2)
      // Le quai reste occupe par T1 : un nouveau ArriveeQuai de T2 doit recevoir Attente.
      quai ! ArriveeQuai(Train2, probeT2.ref)
      probeT2.expectMessage(Attente)
    }

    "etre reutilisable apres un cycle complet" in {
      val quai = spawn(QuaiController())
      val probeT1 = createTestProbe[MessagePourTrain]()

      quai ! ArriveeQuai(Train1, probeT1.ref)
      probeT1.expectMessage(Autorisation)
      quai ! DepartQuai(Train1)

      quai ! ArriveeQuai(Train1, probeT1.ref)
      probeT1.expectMessage(Autorisation)
    }
  }
}
