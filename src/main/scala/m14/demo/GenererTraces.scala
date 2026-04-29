// GenererTraces.scala : produit les fichiers demo/trace-*.json a partir des
// scenarios canoniques (cycle nominal, concurrence canton+quai, tentative PSD invalide).
//
// Lance via : sbt "runMain m14.demo.GenererTraces"
//
// Chaque trace utilise PetriNet.tirer pour calculer les marquages : la simulation
// HTML rejoue donc une sequence d'etats verifiee par le modele Scala.

package m14.demo

import m14.demo.TraceWriter._
import m14.petri.PetriNet._

object GenererTraces extends App {

  // ---------------------------------------------------------------------------
  // Scenario A : cycle nominal complet
  // Train1 : hors -> attente -> canton -> quai -> ouvre -> ferme -> hors
  // ---------------------------------------------------------------------------
  val scenarioA = new Constructeur(
    id = "nominal",
    titre = "Cycle nominal complet (canton + quai + portes)",
    description = "Un seul train. Demande canton -> entree -> arrivee quai -> ouverture portes -> fermeture portes -> depart quai."
  )
    .tirer("Train1 demande l'acces au canton",
      Some(MessageAkka("Train1", "SectionController", "Demande")), t1Demande)
    .tirer("Canton libre : Train1 entre dans le canton",
      Some(MessageAkka("SectionController", "Train1", "Autorisation")), t1EntreeCanton)
    .tirer("Train1 arrive au quai (libere le canton)",
      Some(MessageAkka("Train1", "QuaiController", "ArriveeQuai")), t1ArriveeQuai)
    .tirer("Train1 demande l'ouverture des portes (autorisee : il est a quai)",
      Some(MessageAkka("Train1", "GestionnairePortes", "OuverturePortes")), ouvertureT1)
    .tirer("Train1 demande la fermeture des portes",
      Some(MessageAkka("Train1", "GestionnairePortes", "FermeturePortes")), fermetureT1)
    .tirer("Train1 quitte le quai (autorise : portes fermees)",
      Some(MessageAkka("Train1", "QuaiController", "DepartQuai")), t1DepartQuai)
    .construire()

  // ---------------------------------------------------------------------------
  // Scenario B : concurrence canton + quai
  // Train1 prend canton puis quai. Train2 attend a chaque etape.
  // ---------------------------------------------------------------------------
  val scenarioB = new Constructeur(
    id = "concurrence",
    titre = "Concurrence canton + quai (2 trains)",
    description = "Train1 et Train2 demandent quasi-simultanement. T2 doit attendre canton, puis quai."
  )
    .tirer("Train1 demande le canton",
      Some(MessageAkka("Train1", "SectionController", "Demande")), t1Demande)
    .tirer("Train1 entre dans le canton",
      Some(MessageAkka("SectionController", "Train1", "Autorisation")), t1EntreeCanton)
    .tirer("Train2 demande le canton (sera mis en attente)",
      Some(MessageAkka("Train2", "SectionController", "Demande")), t2Demande)
    .message("Canton occupe : Train2 recoit Attente",
      MessageAkka("SectionController", "Train2", "Attente"))
    .tirer("Train1 arrive au quai (libere le canton)",
      Some(MessageAkka("Train1", "QuaiController", "ArriveeQuai")), t1ArriveeQuai)
    .tirer("Canton libre : Train2 peut enfin entrer",
      Some(MessageAkka("SectionController", "Train2", "Autorisation")), t2EntreeCanton)
    .tirer("Train1 ouvre les portes (a quai)",
      Some(MessageAkka("Train1", "GestionnairePortes", "OuverturePortes")), ouvertureT1)
    .tirer("Train1 ferme les portes",
      Some(MessageAkka("Train1", "GestionnairePortes", "FermeturePortes")), fermetureT1)
    .tirer("Train1 quitte le quai (libere le quai)",
      Some(MessageAkka("Train1", "QuaiController", "DepartQuai")), t1DepartQuai)
    .tirer("Train2 arrive au quai a son tour",
      Some(MessageAkka("Train2", "QuaiController", "ArriveeQuai")), t2ArriveeQuai)
    .construire()

  // ---------------------------------------------------------------------------
  // Scenario C : tentative PSD invalide (CRITIQUE)
  // Un acteur "Attaquant" envoie OuverturePortes alors qu'aucun train n'est a quai.
  // La transition Petri Ouverture_portes_T1 n'est PAS tirable (T1_a_quai = 0).
  // La garde de surete du GestionnairePortes refuse silencieusement.
  // ---------------------------------------------------------------------------
  val scenarioC = new Constructeur(
    id = "violation",
    titre = "Tentative PSD invalide (CRITIQUE - bloquee)",
    description = "Un acteur tente d'ouvrir les portes alors qu'aucun train n'est a quai. Petri refuse (transition non tirable) et la garde Akka refuse silencieusement."
  )
    .violation(
      label = "Attaquant envoie OuverturePortes(Train1) alors qu'aucun train n'est a quai",
      akka = Some(MessageAkka("Attaquant", "GestionnairePortes", "OuverturePortes(Train1)")),
      transition = Some(ouvertureT1)
    )
    .violation(
      label = "Garde de surete : T1_a_quai=0 -> message ignore. Invariant PSD-Open preserve.",
      akka = None,
      transition = None
    )
    .construire()

  // ---------------------------------------------------------------------------
  // Ecriture des 3 fichiers JSON dans demo/.
  // ---------------------------------------------------------------------------
  val sortie = "demo"
  ecrire(scenarioA, s"$sortie/trace-nominal.json")
  ecrire(scenarioB, s"$sortie/trace-concurrence.json")
  ecrire(scenarioC, s"$sortie/trace-violation.json")

  println(s"Traces ecrites dans $sortie/ :")
  println(s"  - trace-nominal.json     (${scenarioA.etapes.size} etapes)")
  println(s"  - trace-concurrence.json (${scenarioB.etapes.size} etapes)")
  println(s"  - trace-violation.json   (${scenarioC.etapes.size} etapes)")
}
