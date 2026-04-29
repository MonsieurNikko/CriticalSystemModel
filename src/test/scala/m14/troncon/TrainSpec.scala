// TrainSpec.scala : tests unitaires de la machine a etats du Train
// (extension PSD : 4 etats hors / attente / sur_canton / a_quai).

package m14.troncon

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import m14.troncon.Protocol._
import org.scalatest.wordspec.AnyWordSpecLike

class TrainSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  // Helper : cree un train avec 3 probes (sectionController, quaiController, gestionnairePortes).
  private def setupTrain(id: IdTrain) = {
    val probeSection = createTestProbe[MessagePourControleur]()
    val probeQuai    = createTestProbe[MessagePourQuai]()
    val probePortes  = createTestProbe[MessagePourPortes]()
    val trainRef     = spawn(Train(id, probeSection.ref, probeQuai.ref, probePortes.ref))
    (trainRef, probeSection, probeQuai, probePortes)
  }

  "Train (extension PSD)" should {

    "envoyer une Demande au SectionController des sa creation" in {
      val (_, probeSection, _, _) = setupTrain(Train1)
      val demande = probeSection.expectMessageType[Demande]
      assert(demande.emetteur == Train1)
    }

    "rester en attente sur message Attente puis progresser sur Autorisation" in {
      val (trainRef, probeSection, probeQuai, _) = setupTrain(Train1)
      probeSection.expectMessageType[Demande]

      trainRef ! Attente
      probeSection.expectNoMessage()
      probeQuai.expectNoMessage()

      trainRef ! Autorisation
      // Apres entree canton, le train envoie immediatement ArriveeQuai au QuaiController.
      val arrivee = probeQuai.expectMessageType[ArriveeQuai]
      assert(arrivee.emetteur == Train1)
    }

    "envoyer ArriveeQuai puis attendre l autorisation du QuaiController" in {
      val (trainRef, probeSection, probeQuai, probePortes) = setupTrain(Train1)
      probeSection.expectMessageType[Demande]
      trainRef ! Autorisation
      probeQuai.expectMessageType[ArriveeQuai]

      // Le quai dit Attente : le train ne doit rien envoyer aux portes.
      trainRef ! Attente
      probePortes.expectNoMessage()
    }

    "envoyer Sortie au SectionController + OuverturePortes apres autorisation quai" in {
      val (trainRef, probeSection, probeQuai, probePortes) = setupTrain(Train1)
      probeSection.expectMessageType[Demande]
      trainRef ! Autorisation
      probeQuai.expectMessageType[ArriveeQuai]

      // Quai accorde -> le train doit liberer le canton (Sortie) puis demander OuverturePortes.
      trainRef ! Autorisation
      val sortie = probeSection.expectMessageType[Sortie]
      assert(sortie.emetteur == Train1)
      val ouverture = probePortes.expectMessageType[OuverturePortes]
      assert(ouverture.emetteur == Train1)
    }

    "envoyer FermeturePortes apres reception PortesOuvertes" in {
      val (trainRef, probeSection, probeQuai, probePortes) = setupTrain(Train1)
      probeSection.expectMessageType[Demande]
      trainRef ! Autorisation
      probeQuai.expectMessageType[ArriveeQuai]
      trainRef ! Autorisation
      probeSection.expectMessageType[Sortie]
      probePortes.expectMessageType[OuverturePortes]

      trainRef ! PortesOuvertes
      val fermeture = probePortes.expectMessageType[FermeturePortes]
      assert(fermeture.emetteur == Train1)
    }

    "envoyer DepartQuai apres reception PortesFermees et redemarrer un cycle" in {
      val (trainRef, probeSection, probeQuai, probePortes) = setupTrain(Train1)
      probeSection.expectMessageType[Demande]
      trainRef ! Autorisation
      probeQuai.expectMessageType[ArriveeQuai]
      trainRef ! Autorisation
      probeSection.expectMessageType[Sortie]
      probePortes.expectMessageType[OuverturePortes]
      trainRef ! PortesOuvertes
      probePortes.expectMessageType[FermeturePortes]

      trainRef ! PortesFermees
      val depart = probeQuai.expectMessageType[DepartQuai]
      assert(depart.emetteur == Train1)

      // Cycle 2 : le train doit re-demander le canton automatiquement.
      val demande2 = probeSection.expectMessageType[Demande]
      assert(demande2.emetteur == Train1)
    }
  }
}
