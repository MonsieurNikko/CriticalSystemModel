// TrainSpec.scala : tests unitaires de la machine a etats du Train.

package m14.troncon

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import m14.troncon.Protocol._
import org.scalatest.wordspec.AnyWordSpecLike

class TrainSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "Train" should {

    "envoyer une Demande au controleur des sa creation" in {
      val probeControleur = createTestProbe[MessagePourControleur]()

      spawn(Train(Train1, probeControleur.ref))

      // Le train doit envoyer Demande(Train1, ...) immediatement.
      val demandeRecue = probeControleur.expectMessageType[Demande]
      assert(demandeRecue.emetteur == Train1)
    }

    "passer sur le troncon et envoyer Sortie apres avoir recu Autorisation" in {
      val probeControleur = createTestProbe[MessagePourControleur]()

      val trainRef = spawn(Train(Train1, probeControleur.ref))

      // Le train envoie Demande a la creation.
      val demandeRecue = probeControleur.expectMessageType[Demande]

      // Le controleur repond Autorisation -> le train doit passer sur le troncon
      // puis envoyer Sortie (transition Ti_sortie_liberation).
      trainRef ! Autorisation
      val sortieRecue = probeControleur.expectMessageType[Sortie]
      assert(sortieRecue.emetteur == Train1)
    }

    "rester en attente apres avoir recu Attente puis progresser sur Autorisation" in {
      val probeControleur = createTestProbe[MessagePourControleur]()

      val trainRef = spawn(Train(Train1, probeControleur.ref))

      // Premiere Demande a la creation.
      probeControleur.expectMessageType[Demande]

      // Le controleur signale Attente -> le train ne doit rien envoyer de plus.
      trainRef ! Attente
      probeControleur.expectNoMessage()

      // Le controleur envoie finalement Autorisation -> le train doit envoyer Sortie.
      trainRef ! Autorisation
      val sortieRecue = probeControleur.expectMessageType[Sortie]
      assert(sortieRecue.emetteur == Train1)
    }

    "recommencer un cycle complet apres etre sorti du troncon" in {
      val probeControleur = createTestProbe[MessagePourControleur]()

      val trainRef = spawn(Train(Train1, probeControleur.ref))

      // Cycle 1 : Demande -> Autorisation -> Sortie
      probeControleur.expectMessageType[Demande]
      trainRef ! Autorisation
      probeControleur.expectMessageType[Sortie]

      // Cycle 2 : le train doit re-demander automatiquement.
      val deuxiemeDemande = probeControleur.expectMessageType[Demande]
      assert(deuxiemeDemande.emetteur == Train1)
    }
  }
}
