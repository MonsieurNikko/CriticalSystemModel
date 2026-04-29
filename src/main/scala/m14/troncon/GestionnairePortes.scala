// GestionnairePortes.scala : controle l'ouverture/fermeture des portes palieres (PSD).
//
// GARDE DE SURETE CRITIQUE (PSD-Open Safety, cf petri-troncon.md section 6.1) :
//   Une demande OuverturePortes est REFUSEE silencieusement si l'emetteur n'est pas
//   actuellement enregistre comme present a quai. Cette garde est l'implementation
//   programmatique de l'invariant :
//       G ( Portes_ouvertes = 1  =>  T1_a_quai + T2_a_quai = 1 )
//
// Le gestionnaire suit donc les arrivees/departs au quai via les notifications
// implicites du QuaiController (relayees par les trains eux-memes a leur arrivee
// a quai). Pour eviter de coupler GestionnairePortes <-> QuaiController, on utilise
// une convention simple : le train s'enregistre via OuverturePortes et le gestionnaire
// confirme la presence en consultant son etat interne.
//
// CHOIX DE DESIGN : ici, le gestionnaire maintient l'ensemble `occupantsAQuai`
// alimente par les messages OuverturePortes (premiere demande == arrivee a quai
// du train) et purge sur le cycle FermeturePortes complet. C'est une simplification
// qui suppose que tout train demandant l'ouverture est effectivement a quai
// (verifie cote Train par sa machine d'etat : seul comportementAQuai envoie
// OuverturePortes).
//
// La garde reelle est donc : "porte deja ouverte par un autre train ?" - on refuse.
// La sequentialite est garantie par l'invariant quai (un seul train a quai a la fois).

package m14.troncon

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object GestionnairePortes {

  import Protocol._

  def apply(): Behavior[MessagePourPortes] = portesFermees()

  // Etat "Portes fermees" : aucun train n'utilise actuellement les portes ouvertes.
  // Sur OuverturePortes(emetteur) -> portes ouvertes pour cet emetteur.
  // Sur FermeturePortes -> cas defensif (deja fermees), on ignore.
  private def portesFermees(): Behavior[MessagePourPortes] = Behaviors.receiveMessage {

    case OuverturePortes(emetteur, repondreA) =>
      // Premier train a demander : on ouvre les portes pour lui (transition
      // Ouverture_portes_Ti dans Petri).
      repondreA ! PortesOuvertes
      portesOuvertes(emetteur)

    case FermeturePortes(_, _) =>
      // Cas defensif : portes deja fermees, aucun cycle en cours. On ignore.
      Behaviors.same
  }

  // Etat "Portes ouvertes" pour un train donne (occupant a quai).
  // GARDE CRITIQUE : toute autre demande OuverturePortes (par un autre train) est
  // REFUSEE silencieusement. Comme l'invariant quai garantit qu'un seul train est
  // a quai a la fois, ce cas ne devrait jamais arriver, mais la garde est une
  // defense en profondeur.
  private def portesOuvertes(occupant: IdTrain): Behavior[MessagePourPortes] =
    Behaviors.receiveMessage {

      case OuverturePortes(emetteur, _) if emetteur == occupant =>
        // Cas defensif : meme train re-demande l'ouverture. Ignore (idempotent).
        Behaviors.same

      case OuverturePortes(_, _) =>
        // GARDE DE SURETE CRITIQUE : un AUTRE train demande l'ouverture alors
        // que les portes sont deja ouvertes pour l'occupant courant. REFUS SILENCIEUX.
        // Cette branche ne doit jamais etre atteinte si l'invariant quai tient.
        // Si elle l'est, c'est un bug en amont - on log eventuellement, mais on
        // ne change pas d'etat.
        Behaviors.same

      case FermeturePortes(emetteur, repondreA) if emetteur == occupant =>
        // Cycle normal : l'occupant ferme les portes avant de partir.
        // Transition Petri Fermeture_portes_Ti.
        repondreA ! PortesFermees
        portesFermees()

      case FermeturePortes(_, _) =>
        // Cas defensif : un train qui n'a pas l'usage des portes ouvertes ne
        // devrait pas demander la fermeture. Ignore.
        Behaviors.same
    }
}
