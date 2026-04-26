// Analyseur.scala : exploration BFS de l'espace d'etats + verification des invariants + detection deadlock.

package m14.petri

import scala.collection.immutable.Queue

object Analyseur {

  import PetriNet._

  // Resultat de l'analyse complete du reseau.
  final case class ResultatAnalyse(
    marquagesAtteignables: List[Marking],
    invariantPrincipalOk: Boolean,
    invariantsParTrainOk: Boolean,
    deadlocks: List[Marking],
    nombreEtats: Int
  )

  // Exploration BFS depuis le marquage initial.
  // Retourne tous les marquages atteignables dans l'ordre de decouverte.
  def explorerEspaceEtats(net: Net): List[Marking] = {
    var visites: Set[Marking] = Set(net.marquageInitial)
    var file: Queue[Marking] = Queue(net.marquageInitial)
    var resultat: List[Marking] = List(net.marquageInitial)

    while (file.nonEmpty) {
      val (marquageCourant, fileRestante) = file.dequeue
      file = fileRestante

      val tirables = transitionsTirables(net, marquageCourant)
      for (transition <- tirables) {
        tirer(transition, marquageCourant) match {
          case Some(nouveauMarquage) =>
            if (!visites.contains(nouveauMarquage)) {
              visites = visites + nouveauMarquage
              file = file.enqueue(nouveauMarquage)
              resultat = resultat :+ nouveauMarquage
            }
          case None =>
            // Ne devrait pas arriver car on a verifie la tirabilite.
            ()
        }
      }
    }

    resultat
  }

  // Verifie l'invariant principal sur un marquage :
  // T1_sur_troncon + T2_sur_troncon + Troncon_libre = 1
  def verifierInvariantPrincipal(marquage: Marking): Boolean = {
    val somme = marquage.getOrElse(T1SurTroncon, 0) +
                marquage.getOrElse(T2SurTroncon, 0) +
                marquage.getOrElse(TronconLibre, 0)
    somme == 1
  }

  // Verifie les invariants par train sur un marquage :
  // Ti_hors + Ti_attente + Ti_sur_troncon = 1 pour chaque train.
  def verifierInvariantsParTrain(marquage: Marking): Boolean = {
    val sommeT1 = marquage.getOrElse(T1Hors, 0) +
                  marquage.getOrElse(T1Attente, 0) +
                  marquage.getOrElse(T1SurTroncon, 0)
    val sommeT2 = marquage.getOrElse(T2Hors, 0) +
                  marquage.getOrElse(T2Attente, 0) +
                  marquage.getOrElse(T2SurTroncon, 0)
    sommeT1 == 1 && sommeT2 == 1
  }

  // Detecte les deadlocks : marquages sans aucune transition tirable.
  def estDeadlock(net: Net, marquage: Marking): Boolean =
    transitionsTirables(net, marquage).isEmpty

  // Analyse complete du reseau : BFS + verification de tous les invariants + deadlocks.
  def analyser(net: Net): ResultatAnalyse = {
    val marquages = explorerEspaceEtats(net)
    val invariantPrincipalOk = marquages.forall(verifierInvariantPrincipal)
    val invariantsParTrainOk = marquages.forall(verifierInvariantsParTrain)
    val deadlocks = marquages.filter(m => estDeadlock(net, m))

    ResultatAnalyse(
      marquagesAtteignables = marquages,
      invariantPrincipalOk = invariantPrincipalOk,
      invariantsParTrainOk = invariantsParTrainOk,
      deadlocks = deadlocks,
      nombreEtats = marquages.size
    )
  }

  // Point d'entree pour lancer l'analyse depuis la ligne de commande : sbt "runMain m14.petri.Analyseur"
  def main(args: Array[String]): Unit = {
    println("=== Analyseur Petri - Troncon partage M14 ===")
    println()

    val resultat = analyser(reseauTroncon)

    println(s"Nombre de marquages atteignables : ${resultat.nombreEtats}")
    println()

    // Affichage de chaque marquage avec notation compacte (places marquees a 1).
    println("--- Marquages atteignables ---")
    resultat.marquagesAtteignables.zipWithIndex.foreach { case (marquage, index) =>
      val placesActives = marquage.filter(_._2 > 0).keys.toList.sorted.mkString(", ")
      val invariantOk = if (verifierInvariantPrincipal(marquage)) "OK" else "VIOLATION"
      println(s"  M$index = ($placesActives) [invariant: $invariantOk]")
    }
    println()

    // Invariant principal.
    val statusInvariant = if (resultat.invariantPrincipalOk) "PASSE" else "ECHEC"
    println(s"Invariant principal (T1_sur + T2_sur + libre = 1) : $statusInvariant")

    // Invariants par train.
    val statusTrains = if (resultat.invariantsParTrainOk) "PASSE" else "ECHEC"
    println(s"Invariants par train (Ti_hors + Ti_att + Ti_sur = 1) : $statusTrains")

    // Deadlocks.
    if (resultat.deadlocks.isEmpty) {
      println("Deadlocks : AUCUN (le systeme peut toujours progresser)")
    } else {
      println(s"Deadlocks : ${resultat.deadlocks.size} DETECTE(S) !")
      resultat.deadlocks.foreach { m =>
        val placesActives = m.filter(_._2 > 0).keys.toList.sorted.mkString(", ")
        println(s"  DEADLOCK : ($placesActives)")
      }
    }

    // Exclusion mutuelle.
    val collisionDetectee = resultat.marquagesAtteignables.exists { m =>
      m.getOrElse(T1SurTroncon, 0) >= 1 && m.getOrElse(T2SurTroncon, 0) >= 1
    }
    val statusExclusion = if (!collisionDetectee) "PASSE" else "ECHEC - COLLISION DETECTEE"
    println(s"Exclusion mutuelle (jamais T1 et T2 ensemble) : $statusExclusion")

    println()
    println("=== Fin de l'analyse ===")
  }
}
