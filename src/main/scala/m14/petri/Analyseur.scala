// Analyseur.scala : exploration BFS de l'espace d'etats + verification des 5 invariants
// + detection deadlock. Modele etendu PSD (cf petri-troncon.md sections 5 et 6).

package m14.petri

import scala.collection.immutable.Queue

object Analyseur {

  import PetriNet._

  // Resultat complet d'une analyse du reseau.
  // 5 invariants sont verifies sur tous les marquages atteignables :
  //   1. Invariant canton  (5.1) : T1_sur_canton + T2_sur_canton + Canton_libre = 1
  //   2. Invariant quai    (5.2) : T1_a_quai + T2_a_quai + Quai_libre = 1
  //   3. Invariant portes  (5.3) : Portes_fermees + Portes_ouvertes = 1
  //   4. PSD-Open Safety   (6.1) : Portes_ouvertes = 1 => T1_a_quai + T2_a_quai = 1  [CRITIQUE]
  //   5. PSD-Departure     (6.2) : Ti_depart_quai tirable => Portes_fermees = 1     [CRITIQUE]
  // Plus les invariants par train : Ti_hors + Ti_attente + Ti_sur_canton + Ti_a_quai = 1.
  final case class ResultatAnalyse(
    marquagesAtteignables: List[Marking],
    invariantCantonOk: Boolean,
    invariantQuaiOk: Boolean,
    invariantPortesOk: Boolean,
    invariantPsdOpenOk: Boolean,
    invariantPsdDepartureOk: Boolean,
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
          case None => ()
        }
      }
    }

    resultat
  }

  // ===========================================================================
  // Invariants de ressource (P-invariants)
  // ===========================================================================

  // 5.1 - Invariant canton : T1_sur_canton + T2_sur_canton + Canton_libre = 1
  def verifierInvariantCanton(marquage: Marking): Boolean = {
    val somme = marquage.getOrElse(T1SurCanton, 0) +
                marquage.getOrElse(T2SurCanton, 0) +
                marquage.getOrElse(CantonLibre, 0)
    somme == 1
  }

  // 5.2 - Invariant quai : T1_a_quai + T2_a_quai + Quai_libre = 1
  def verifierInvariantQuai(marquage: Marking): Boolean = {
    val somme = marquage.getOrElse(T1AQuai, 0) +
                marquage.getOrElse(T2AQuai, 0) +
                marquage.getOrElse(QuaiLibre, 0)
    somme == 1
  }

  // 5.3 - Invariant portes : Portes_fermees + Portes_ouvertes = 1
  def verifierInvariantPortes(marquage: Marking): Boolean = {
    val somme = marquage.getOrElse(PortesFermees, 0) +
                marquage.getOrElse(PortesOuvertes, 0)
    somme == 1
  }

  // Invariants par train : Ti_hors + Ti_attente + Ti_sur_canton + Ti_a_quai = 1
  def verifierInvariantsParTrain(marquage: Marking): Boolean = {
    val sommeT1 = marquage.getOrElse(T1Hors, 0) +
                  marquage.getOrElse(T1Attente, 0) +
                  marquage.getOrElse(T1SurCanton, 0) +
                  marquage.getOrElse(T1AQuai, 0)
    val sommeT2 = marquage.getOrElse(T2Hors, 0) +
                  marquage.getOrElse(T2Attente, 0) +
                  marquage.getOrElse(T2SurCanton, 0) +
                  marquage.getOrElse(T2AQuai, 0)
    sommeT1 == 1 && sommeT2 == 1
  }

  // ===========================================================================
  // Invariants critiques de surete PSD
  // ===========================================================================

  // 6.1 - PSD-Open Safety [CRITIQUE] :
  // Portes_ouvertes = 1 => T1_a_quai + T2_a_quai = 1
  // (si les portes palieres sont ouvertes, un train doit etre a quai).
  def verifierSurteOuverturePortes(marquage: Marking): Boolean = {
    val portesOuvertes = marquage.getOrElse(PortesOuvertes, 0)
    if (portesOuvertes >= 1) {
      val sommeAQuai = marquage.getOrElse(T1AQuai, 0) + marquage.getOrElse(T2AQuai, 0)
      sommeAQuai == 1
    } else {
      true
    }
  }

  // 6.2 - PSD-Departure Safety [CRITIQUE] :
  // Pour tout marquage M, si Ti_depart_quai est tirable, alors M(Portes_fermees) = 1.
  // Cette propriete est garantie structurellement par le pre de la transition,
  // mais on la verifie programmatiquement par defense en profondeur.
  def verifierSurteDepartQuai(net: Net, marquage: Marking): Boolean = {
    val transitionsDepart = List(t1DepartQuai, t2DepartQuai)
    transitionsDepart.forall { t =>
      if (estTirable(t, marquage)) marquage.getOrElse(PortesFermees, 0) == 1
      else true
    }
  }

  // ===========================================================================
  // Deadlock + analyse complete
  // ===========================================================================

  def estDeadlock(net: Net, marquage: Marking): Boolean =
    transitionsTirables(net, marquage).isEmpty

  def analyser(net: Net): ResultatAnalyse = {
    val marquages = explorerEspaceEtats(net)
    ResultatAnalyse(
      marquagesAtteignables = marquages,
      invariantCantonOk = marquages.forall(verifierInvariantCanton),
      invariantQuaiOk = marquages.forall(verifierInvariantQuai),
      invariantPortesOk = marquages.forall(verifierInvariantPortes),
      invariantPsdOpenOk = marquages.forall(verifierSurteOuverturePortes),
      invariantPsdDepartureOk = marquages.forall(m => verifierSurteDepartQuai(net, m)),
      invariantsParTrainOk = marquages.forall(verifierInvariantsParTrain),
      deadlocks = marquages.filter(m => estDeadlock(net, m)),
      nombreEtats = marquages.size
    )
  }

  // ===========================================================================
  // Point d'entree : sbt "runMain m14.petri.Analyseur"
  // ===========================================================================
  def main(args: Array[String]): Unit = {
    println("=== Analyseur Petri - Canton + Quai + Portes palieres (M14) ===")
    println()

    val resultat = analyser(reseauTroncon)

    println(s"Nombre de marquages atteignables : ${resultat.nombreEtats}")
    println()

    println("--- Marquages atteignables ---")
    resultat.marquagesAtteignables.zipWithIndex.foreach { case (marquage, index) =>
      val placesActives = marquage.filter(_._2 > 0).keys.toList.sorted.mkString(", ")
      println(s"  M$index = ($placesActives)")
    }
    println()

    println("--- Invariants ---")
    println(s"  Invariant canton  (5.1)         : ${statut(resultat.invariantCantonOk)}")
    println(s"  Invariant quai    (5.2)         : ${statut(resultat.invariantQuaiOk)}")
    println(s"  Invariant portes  (5.3)         : ${statut(resultat.invariantPortesOk)}")
    println(s"  Invariants par train            : ${statut(resultat.invariantsParTrainOk)}")
    println(s"  PSD-Open Safety  (6.1) CRITIQUE : ${statut(resultat.invariantPsdOpenOk)}")
    println(s"  PSD-Departure    (6.2) CRITIQUE : ${statut(resultat.invariantPsdDepartureOk)}")
    println()

    if (resultat.deadlocks.isEmpty) {
      println("Deadlocks : AUCUN (le systeme peut toujours progresser)")
    } else {
      println(s"Deadlocks : ${resultat.deadlocks.size} DETECTE(S) !")
      resultat.deadlocks.foreach { m =>
        val placesActives = m.filter(_._2 > 0).keys.toList.sorted.mkString(", ")
        println(s"  DEADLOCK : ($placesActives)")
      }
    }

    println()
    val collisionCanton = resultat.marquagesAtteignables.exists { m =>
      m.getOrElse(T1SurCanton, 0) >= 1 && m.getOrElse(T2SurCanton, 0) >= 1
    }
    val collisionQuai = resultat.marquagesAtteignables.exists { m =>
      m.getOrElse(T1AQuai, 0) >= 1 && m.getOrElse(T2AQuai, 0) >= 1
    }
    println(s"Exclusion mutuelle canton : ${statut(!collisionCanton)}")
    println(s"Exclusion mutuelle quai   : ${statut(!collisionQuai)}")

    println()
    println("=== Fin de l'analyse ===")
  }

  private def statut(ok: Boolean): String = if (ok) "PASSE" else "ECHEC"
}
