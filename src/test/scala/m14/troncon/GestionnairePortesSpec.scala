// GestionnairePortesSpec.scala : tests unitaires de la garde de surete PSD-Open.
// Test critique : seul le train present a quai peut ouvrir les portes.

package m14.troncon

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import m14.troncon.Protocol._
import org.scalatest.wordspec.AnyWordSpecLike

class GestionnairePortesSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "GestionnairePortes" should {

    "ouvrir les portes a la demande du premier train (etat ferme)" in {
      val gp = spawn(GestionnairePortes())
      val probeT1 = createTestProbe[MessagePourTrain]()

      gp ! OuverturePortes(Train1, probeT1.ref)
      probeT1.expectMessage(PortesOuvertes)
    }

    "fermer les portes apres demande du train occupant" in {
      val gp = spawn(GestionnairePortes())
      val probeT1 = createTestProbe[MessagePourTrain]()

      gp ! OuverturePortes(Train1, probeT1.ref)
      probeT1.expectMessage(PortesOuvertes)

      gp ! FermeturePortes(Train1, probeT1.ref)
      probeT1.expectMessage(PortesFermees)
    }

    "REFUSER SILENCIEUSEMENT OuverturePortes d un autre train (PSD-Open Safety)" in {
      // CRITIQUE : c'est l'invariant 6.1 du modele Petri.
      // Quand les portes sont deja ouvertes pour T1, T2 ne doit JAMAIS reussir a
      // les ouvrir aussi (il n'est pas l'occupant).
      val gp = spawn(GestionnairePortes())
      val probeT1 = createTestProbe[MessagePourTrain]()
      val probeT2 = createTestProbe[MessagePourTrain]()

      gp ! OuverturePortes(Train1, probeT1.ref)
      probeT1.expectMessage(PortesOuvertes)

      // T2 envoie OuverturePortes : doit etre ignore silencieusement.
      gp ! OuverturePortes(Train2, probeT2.ref)
      probeT2.expectNoMessage()
    }

    "REFUSER SILENCIEUSEMENT FermeturePortes d un train non occupant" in {
      val gp = spawn(GestionnairePortes())
      val probeT1 = createTestProbe[MessagePourTrain]()
      val probeT2 = createTestProbe[MessagePourTrain]()

      gp ! OuverturePortes(Train1, probeT1.ref)
      probeT1.expectMessage(PortesOuvertes)

      // T2 tente de fermer : doit etre ignore.
      gp ! FermeturePortes(Train2, probeT2.ref)
      probeT2.expectNoMessage()

      // T1 (vrai occupant) peut toujours fermer.
      gp ! FermeturePortes(Train1, probeT1.ref)
      probeT1.expectMessage(PortesFermees)
    }

    "etre reutilisable apres un cycle complet (ouvre/ferme par train different)" in {
      val gp = spawn(GestionnairePortes())
      val probeT1 = createTestProbe[MessagePourTrain]()
      val probeT2 = createTestProbe[MessagePourTrain]()

      // Cycle T1
      gp ! OuverturePortes(Train1, probeT1.ref)
      probeT1.expectMessage(PortesOuvertes)
      gp ! FermeturePortes(Train1, probeT1.ref)
      probeT1.expectMessage(PortesFermees)

      // Cycle T2 : maintenant les portes sont fermees, T2 peut donc les ouvrir.
      gp ! OuverturePortes(Train2, probeT2.ref)
      probeT2.expectMessage(PortesOuvertes)
      gp ! FermeturePortes(Train2, probeT2.ref)
      probeT2.expectMessage(PortesFermees)
    }
  }
}
