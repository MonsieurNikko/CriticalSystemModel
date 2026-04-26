// AnalyseurSpec.scala : tests de l'analyseur Petri sur le reseau du troncon partage.

package m14.petri

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class AnalyseurSpec extends AnyWordSpec with Matchers {

  import PetriNet._
  import Analyseur._

  "PetriNet" should {

    "avoir 7 places et 6 transitions" in {
      toutesLesPlaces.size shouldBe 7
      toutesLesTransitions.size shouldBe 6
    }

    "avoir un marquage initial coherent (3 jetons total)" in {
      val totalJetons = marquageInitial.values.sum
      totalJetons shouldBe 3
    }

    "rendre T1_demande tirable depuis M0" in {
      estTirable(t1Demande, marquageInitial) shouldBe true
    }

    "rendre T2_demande tirable depuis M0" in {
      estTirable(t2Demande, marquageInitial) shouldBe true
    }

    "ne pas rendre T1_entree_autorisee tirable depuis M0" in {
      // Pas tirable car T1_attente = 0 dans M0.
      estTirable(t1EntreeAutorisee, marquageInitial) shouldBe false
    }

    "calculer correctement le marquage apres T1_demande depuis M0" in {
      val resultat = tirer(t1Demande, marquageInitial)
      resultat shouldBe defined

      val m1 = resultat.get
      m1(T1Hors) shouldBe 0
      m1(T1Attente) shouldBe 1
      m1(TronconLibre) shouldBe 1  // Troncon toujours libre apres une simple demande.
    }

    "calculer correctement le marquage apres T1_demande puis T1_entree_autorisee" in {
      val apresT1Demande = tirer(t1Demande, marquageInitial).get
      val apresT1Entree = tirer(t1EntreeAutorisee, apresT1Demande).get

      apresT1Entree(T1Attente) shouldBe 0
      apresT1Entree(T1SurTroncon) shouldBe 1
      apresT1Entree(TronconLibre) shouldBe 0  // Troncon occupe par T1.
    }
  }

  "Analyseur" should {

    "trouver exactement 8 marquages atteignables" in {
      val resultat = analyser(reseauTroncon)
      resultat.nombreEtats shouldBe 8
    }

    "verifier l'invariant principal sur tous les marquages" in {
      val resultat = analyser(reseauTroncon)
      resultat.invariantPrincipalOk shouldBe true
    }

    "verifier les invariants par train sur tous les marquages" in {
      val resultat = analyser(reseauTroncon)
      resultat.invariantsParTrainOk shouldBe true
    }

    "ne detecter aucun deadlock" in {
      val resultat = analyser(reseauTroncon)
      resultat.deadlocks shouldBe empty
    }

    "garantir l'exclusion mutuelle (jamais T1 et T2 sur le troncon en meme temps)" in {
      val resultat = analyser(reseauTroncon)
      val collision = resultat.marquagesAtteignables.exists { m =>
        m.getOrElse(T1SurTroncon, 0) >= 1 && m.getOrElse(T2SurTroncon, 0) >= 1
      }
      collision shouldBe false
    }
  }
}
