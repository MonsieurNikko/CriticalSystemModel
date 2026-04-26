// SectionControllerSpec.scala : tests des 3 scenarios critiques du troncon partage.

package m14.troncon

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import m14.troncon.Protocol._
import org.scalatest.wordspec.AnyWordSpecLike

class SectionControllerSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "SectionController" should {

    // Scenario 1 - Nominal (cf comparaison.md section 2) :
    // Un seul train demande, obtient l'autorisation, occupe le troncon, sort, le troncon revient libre.
    "autoriser un train seul puis revenir a l'etat libre apres sa sortie" in {
      val controleur = spawn(SectionController())
      val probeTrain1 = createTestProbe[MessagePourTrain]()

      // Train1 demande l'acces au troncon libre -> doit recevoir Autorisation.
      controleur ! Demande(Train1, probeTrain1.ref)
      probeTrain1.expectMessage(Autorisation)

      // Train1 sort du troncon.
      controleur ! Sortie(Train1)

      // Verification que le troncon est bien libre : un nouveau Train2 doit obtenir Autorisation.
      val probeTrain2 = createTestProbe[MessagePourTrain]()
      controleur ! Demande(Train2, probeTrain2.ref)
      probeTrain2.expectMessage(Autorisation)
    }

    // Scenario 2 - Concurrence (cf comparaison.md section 3) :
    // Deux trains demandent, le premier obtient Autorisation, le second recoit Attente.
    "autoriser le premier train et mettre le second en attente lors d'une demande concurrente" in {
      val controleur = spawn(SectionController())
      val probeTrain1 = createTestProbe[MessagePourTrain]()
      val probeTrain2 = createTestProbe[MessagePourTrain]()

      // Train1 demande en premier -> Autorisation (troncon libre).
      controleur ! Demande(Train1, probeTrain1.ref)
      probeTrain1.expectMessage(Autorisation)

      // Train2 demande pendant que Train1 occupe -> Attente.
      controleur ! Demande(Train2, probeTrain2.ref)
      probeTrain2.expectMessage(Attente)
    }

    // Scenario 3 - Liberation / Progression (cf comparaison.md section 4) :
    // Le train occupant sort, le train en attente recoit Autorisation sans deadlock.
    "promouvoir le train en attente quand l'occupant sort du troncon" in {
      val controleur = spawn(SectionController())
      val probeTrain1 = createTestProbe[MessagePourTrain]()
      val probeTrain2 = createTestProbe[MessagePourTrain]()

      // Train1 entre sur le troncon.
      controleur ! Demande(Train1, probeTrain1.ref)
      probeTrain1.expectMessage(Autorisation)

      // Train2 demande et recoit Attente.
      controleur ! Demande(Train2, probeTrain2.ref)
      probeTrain2.expectMessage(Attente)

      // Train1 sort -> le controleur doit promouvoir Train2.
      controleur ! Sortie(Train1)
      probeTrain2.expectMessage(Autorisation)

      // Verification finale : Train2 sort et le troncon est libre pour un prochain.
      controleur ! Sortie(Train2)
      val probeVerification = createTestProbe[MessagePourTrain]()
      controleur ! Demande(Train1, probeVerification.ref)
      probeVerification.expectMessage(Autorisation)
    }
  }
}
