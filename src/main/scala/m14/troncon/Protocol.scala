// Protocol.scala : types de messages echanges entre les Trains, le SectionController,
// le QuaiController et le GestionnairePortes (extension PSD - canton + quai + portes palieres).

package m14.troncon

import akka.actor.typed.ActorRef

object Protocol {

  // Identifiant d'un train. Le coeur du projet en utilise exactement deux.
  sealed trait IdTrain
  case object Train1 extends IdTrain
  case object Train2 extends IdTrain

  // ---------------------------------------------------------------------------
  // Messages que le SectionController peut recevoir.
  // Inchanges depuis le modele initial (cf petri-troncon.md transitions Ti_demande,
  // Ti_entree_canton, Ti_arrivee_quai cote canton, Ti_depart_quai indirect).
  // ---------------------------------------------------------------------------
  sealed trait MessagePourControleur

  final case class Demande(emetteur: IdTrain, repondreA: ActorRef[MessagePourTrain]) extends MessagePourControleur
  final case class Sortie(emetteur: IdTrain) extends MessagePourControleur

  // ---------------------------------------------------------------------------
  // Messages que le QuaiController peut recevoir (NOUVEAUX - extension PSD).
  // ArriveeQuai = demande d'acces au quai (transition Ti_arrivee_quai).
  // DepartQuai  = liberation du quai (transition Ti_depart_quai).
  // ---------------------------------------------------------------------------
  sealed trait MessagePourQuai

  final case class ArriveeQuai(emetteur: IdTrain, repondreA: ActorRef[MessagePourTrain]) extends MessagePourQuai
  final case class DepartQuai(emetteur: IdTrain) extends MessagePourQuai

  // ---------------------------------------------------------------------------
  // Messages que le GestionnairePortes peut recevoir (NOUVEAUX - extension PSD).
  // OuverturePortes : demande d'ouverture, REFUSEE silencieusement par la garde
  //                   de surete si l'emetteur n'est pas a quai (CRITIQUE PSD-Open).
  // FermeturePortes : demande de fermeture (acceptee si portes ouvertes pour ce train).
  // ---------------------------------------------------------------------------
  sealed trait MessagePourPortes

  final case class OuverturePortes(emetteur: IdTrain, repondreA: ActorRef[MessagePourTrain]) extends MessagePourPortes
  final case class FermeturePortes(emetteur: IdTrain, repondreA: ActorRef[MessagePourTrain]) extends MessagePourPortes

  // ---------------------------------------------------------------------------
  // Messages que le SectionController, le QuaiController et le GestionnairePortes
  // peuvent envoyer a un Train.
  // ---------------------------------------------------------------------------
  sealed trait MessagePourTrain

  // Reponses des controleurs (canton et quai)
  case object Autorisation extends MessagePourTrain
  case object Attente extends MessagePourTrain

  // Reponses du gestionnaire de portes (acquittement de fin de cycle)
  case object PortesOuvertes extends MessagePourTrain
  case object PortesFermees extends MessagePourTrain
}
