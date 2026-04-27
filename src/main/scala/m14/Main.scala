// Main.scala : demonstration console des scenarios critiques et du modele Petri.

package m14

import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import m14.petri.Analyseur
import m14.petri.PetriNet
import m14.petri.PetriNet._
import m14.troncon.Protocol._
import m14.troncon.SectionController

object Main extends App {

  // Acteur de log : se comporte comme un faux train qui ne fait qu'afficher les reponses
  // recues du controleur. Sert a rendre la sortie console lisible.
  // On evite d'utiliser le vrai acteur Train pour ne pas tomber dans son cycle infini
  // (hors -> demande -> attente -> sur -> sortie -> hors -> ...).
  private def loggeur(nom: String): Behavior[MessagePourTrain] =
    Behaviors.receiveMessage {
      case Autorisation =>
        println(s"  [$nom] recoit Autorisation")
        Behaviors.same
      case Attente =>
        println(s"  [$nom] recoit Attente")
        Behaviors.same
    }

  private def afficherMarquage(marquage: Marking): String =
    marquage.filter(_._2 > 0).keys.toList.sorted.mkString("(", ", ", ")")

  private def appliquerTransition(numero: Int, marquage: Marking, transition: Transition): Marking = {
    val nouveauMarquage = PetriNet.tirer(transition, marquage).getOrElse {
      throw new IllegalStateException(s"Transition non tirable dans la demo : ${transition.nom}")
    }

    println(s"  Petri M$numero --${transition.nom}--> ${afficherMarquage(nouveauMarquage)}")
    nouveauMarquage
  }

  private def afficherBilanPetri(): Unit = {
    val resultat = Analyseur.analyser(PetriNet.reseauTroncon)
    val collisionDetectee = resultat.marquagesAtteignables.exists { marquage =>
      marquage.getOrElse(T1SurTroncon, 0) == 1 && marquage.getOrElse(T2SurTroncon, 0) == 1
    }

    println("--- Bilan automatique Petri ---")
    println(s"  Etats atteignables : ${resultat.nombreEtats}")
    println(s"  Invariant ressource : ${if (resultat.invariantPrincipalOk) "OK" else "ECHEC"}")
    println(s"  Invariants trains : ${if (resultat.invariantsParTrainOk) "OK" else "ECHEC"}")
    println(s"  Deadlocks : ${resultat.deadlocks.size}")
    println(s"  Collision detectee : ${if (collisionDetectee) "OUI" else "NON"}")
    println()
  }

  // Acteur racine : execute les 3 scenarios l'un apres l'autre.
  // On utilise des Thread.sleep entre les etapes parce qu'on veut une sortie console
  // deterministe pour le lecteur. C'est volontairement simple : ce n'est pas un test
  // (les vrais tests sont dans SectionControllerSpec), c'est une demo visuelle.
  private def demo(): Behavior[Unit] = Behaviors.setup { contexte =>

    println("=== Demonstration des 3 scenarios du troncon partage M14 ===")
    println()
    println(s"Marquage initial Petri : ${afficherMarquage(marquageInitial)}")
    println()

    // Scenario 1 - Nominal : un seul train demande, obtient, sort.
    println("--- Scenario 1 - Nominal ---")
    val controleur1 = contexte.spawn(SectionController(), "controleur-1")
    val log1 = contexte.spawn(loggeur("Train1"), "log-train1-scenario1")
    var marquageScenario1 = marquageInitial
    println("  Train1 envoie Demande")
    controleur1 ! Demande(Train1, log1)
    marquageScenario1 = appliquerTransition(1, marquageScenario1, t1Demande)
    Thread.sleep(150)
    marquageScenario1 = appliquerTransition(2, marquageScenario1, t1EntreeAutorisee)
    Thread.sleep(150)
    println("  Train1 envoie Sortie")
    controleur1 ! Sortie(Train1)
    marquageScenario1 = appliquerTransition(3, marquageScenario1, t1SortiLiberation)
    Thread.sleep(150)
    println()

    // Scenario 2 - Concurrence : deux trains demandent, le premier obtient, le second attend.
    println("--- Scenario 2 - Concurrence ---")
    val controleur2 = contexte.spawn(SectionController(), "controleur-2")
    val log2a = contexte.spawn(loggeur("Train1"), "log-train1-scenario2")
    val log2b = contexte.spawn(loggeur("Train2"), "log-train2-scenario2")
    var marquageScenario2 = marquageInitial
    println("  Train1 envoie Demande")
    controleur2 ! Demande(Train1, log2a)
    marquageScenario2 = appliquerTransition(1, marquageScenario2, t1Demande)
    Thread.sleep(150)
    marquageScenario2 = appliquerTransition(2, marquageScenario2, t1EntreeAutorisee)
    Thread.sleep(150)
    println("  Train2 envoie Demande")
    controleur2 ! Demande(Train2, log2b)
    marquageScenario2 = appliquerTransition(3, marquageScenario2, t2Demande)
    println("  Petri : le message Attente ne change pas le marquage")
    Thread.sleep(150)
    println()

    // Scenario 3 - Liberation / Progression : l'occupant sort, l'attendant progresse.
    println("--- Scenario 3 - Liberation / Progression ---")
    val controleur3 = contexte.spawn(SectionController(), "controleur-3")
    val log3a = contexte.spawn(loggeur("Train1"), "log-train1-scenario3")
    val log3b = contexte.spawn(loggeur("Train2"), "log-train2-scenario3")
    var marquageScenario3 = marquageInitial
    println("  Train1 envoie Demande")
    controleur3 ! Demande(Train1, log3a)
    marquageScenario3 = appliquerTransition(1, marquageScenario3, t1Demande)
    Thread.sleep(150)
    marquageScenario3 = appliquerTransition(2, marquageScenario3, t1EntreeAutorisee)
    Thread.sleep(150)
    println("  Train2 envoie Demande")
    controleur3 ! Demande(Train2, log3b)
    marquageScenario3 = appliquerTransition(3, marquageScenario3, t2Demande)
    println("  Petri : le message Attente ne change pas le marquage")
    Thread.sleep(150)
    println("  Train1 envoie Sortie -> Train2 doit etre promu")
    controleur3 ! Sortie(Train1)
    marquageScenario3 = appliquerTransition(4, marquageScenario3, t1SortiLiberation)
    Thread.sleep(150)
    marquageScenario3 = appliquerTransition(5, marquageScenario3, t2EntreeAutorisee)
    Thread.sleep(150)
    println("  Train2 envoie Sortie")
    controleur3 ! Sortie(Train2)
    marquageScenario3 = appliquerTransition(6, marquageScenario3, t2SortiLiberation)
    Thread.sleep(150)
    println()

    afficherBilanPetri()
    println("=== Fin de la demonstration ===")
    Behaviors.stopped
  }

  val system: ActorSystem[Unit] = ActorSystem(demo(), "demo-troncon-partage")
  // On laisse le temps a la demo de se terminer avant d'arreter le systeme.
  Thread.sleep(2000)
  system.terminate()
}
