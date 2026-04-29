// TraceWriter.scala : producteur de traces JSON pour la simulation animee (demo/index.html).
//
// Une trace = liste d'evenements scripted, chacun decrivant :
//   - un libelle humain (affiche dans la timeline)
//   - eventuellement un message Akka (popup)
//   - eventuellement le tir d'une transition Petri (jeton qui se deplace)
//   - le marquage Petri courant (12 places) -> reconstitue cote HTML pour positionner
//     les trains, les portes et les jetons
//   - un drapeau "violation" pour les tentatives bloquees par les gardes de surete
//
// Aucune dependance externe : serialisation JSON manuelle (~30 lignes).
// La trace EST construite a partir du vrai reseau Petri (PetriNet.tirer), ce qui
// garantit que l'animation reflete le modele Scala verifie.

package m14.demo

import m14.petri.PetriNet
import m14.petri.PetriNet.{Marking, Transition}

import java.nio.file.{Files, Paths}
import java.nio.charset.StandardCharsets

object TraceWriter {

  // Un message Akka echange entre un emetteur et un controleur.
  final case class MessageAkka(de: String, vers: String, libelle: String)

  // Une etape de la trace.
  // - label : phrase affichee dans la timeline
  // - akka  : message Akka eventuel (popup en bas d'ecran)
  // - transition : nom de la transition Petri tiree (None si pas de tir)
  // - marquage   : marquage resultant (12 places)
  // - violation  : true si la transition n'etait PAS tirable (mode demo PSD invalide)
  //                ou si la garde de surete a refuse silencieusement le message Akka
  final case class Etape(
    label: String,
    akka: Option[MessageAkka],
    transition: Option[String],
    marquage: Marking,
    violation: Boolean
  )

  // Une trace complete = un scenario.
  final case class Trace(id: String, titre: String, description: String, etapes: List[Etape])

  // ---------------------------------------------------------------------------
  // Constructeur fluide d'une trace : on enchaine .ajouter(...) en partant
  // du marquage initial.
  // ---------------------------------------------------------------------------
  final class Constructeur(id: String, titre: String, description: String) {
    private var marquageCourant: Marking = PetriNet.marquageInitial
    private val etapes = scala.collection.mutable.ListBuffer.empty[Etape]

    // Etape initiale : etat M0 sans message ni transition.
    etapes += Etape(
      label = "Etat initial M0",
      akka = None,
      transition = None,
      marquage = marquageCourant,
      violation = false
    )

    // Tire une transition Petri et enregistre l'evenement (avec eventuellement un message Akka).
    def tirer(label: String, akka: Option[MessageAkka], transition: Transition): this.type = {
      PetriNet.tirer(transition, marquageCourant) match {
        case Some(nouveau) =>
          marquageCourant = nouveau
          etapes += Etape(label, akka, Some(transition.nom), marquageCourant, violation = false)
        case None =>
          // Cas non attendu dans un scenario valide : on enregistre une violation.
          etapes += Etape(label, akka, Some(transition.nom), marquageCourant, violation = true)
      }
      this
    }

    // Note un message Akka qui ne provoque PAS de transition Petri (ex: notification Attente).
    def message(label: String, akka: MessageAkka): this.type = {
      etapes += Etape(label, Some(akka), None, marquageCourant, violation = false)
      this
    }

    // Note une violation : message Akka envoye mais REFUSE par une garde,
    // ou transition Petri non tirable. Le marquage ne change pas.
    def violation(label: String, akka: Option[MessageAkka], transition: Option[Transition]): this.type = {
      etapes += Etape(label, akka, transition.map(_.nom), marquageCourant, violation = true)
      this
    }

    def construire(): Trace = Trace(id, titre, description, etapes.toList)
  }

  // ---------------------------------------------------------------------------
  // Serialisation JSON minimale (sans dependance externe).
  // ---------------------------------------------------------------------------
  private def echapper(s: String): String =
    s.flatMap {
      case '"'  => "\\\""
      case '\\' => "\\\\"
      case '\n' => "\\n"
      case '\r' => "\\r"
      case '\t' => "\\t"
      case c    => c.toString
    }

  private def chaine(s: String): String = "\"" + echapper(s) + "\""

  private def jsonMessage(m: MessageAkka): String =
    s"""{"de": ${chaine(m.de)}, "vers": ${chaine(m.vers)}, "libelle": ${chaine(m.libelle)}}"""

  private def jsonMarquage(m: Marking): String = {
    val ordre = List(
      PetriNet.CantonLibre, PetriNet.QuaiLibre, PetriNet.PortesFermees, PetriNet.PortesOuvertes,
      PetriNet.T1Hors, PetriNet.T1Attente, PetriNet.T1SurCanton, PetriNet.T1AQuai,
      PetriNet.T2Hors, PetriNet.T2Attente, PetriNet.T2SurCanton, PetriNet.T2AQuai
    )
    ordre.map(p => s"${chaine(p)}: ${m.getOrElse(p, 0)}").mkString("{", ", ", "}")
  }

  // Une etape rendue sur plusieurs lignes (4 espaces d'indentation = 2 niveaux).
  private def jsonEtape(e: Etape): String = {
    val champs = List(
      Some(s""""label": ${chaine(e.label)}"""),
      e.akka.map(a => s""""akka": ${jsonMessage(a)}"""),
      e.transition.map(t => s""""transition": ${chaine(t)}"""),
      Some(s""""marquage": ${jsonMarquage(e.marquage)}"""),
      Some(s""""violation": ${e.violation}""")
    ).flatten
    champs.map("      " + _).mkString("{\n", ",\n", "\n    }")
  }

  // Trace complete avec mise en forme lisible.
  def jsonTrace(t: Trace): String = {
    val etapesJson = t.etapes.map(jsonEtape).mkString("[\n    ", ",\n    ", "\n  ]")
    s"""{
  "id": ${chaine(t.id)},
  "titre": ${chaine(t.titre)},
  "description": ${chaine(t.description)},
  "etapes": $etapesJson
}
"""
  }

  // Ecrit une trace dans un fichier (cree les dossiers parents si besoin).
  def ecrire(trace: Trace, chemin: String): Unit = {
    val path = Paths.get(chemin)
    Option(path.getParent).foreach(Files.createDirectories(_))
    Files.write(path, jsonTrace(trace).getBytes(StandardCharsets.UTF_8))
  }
}
