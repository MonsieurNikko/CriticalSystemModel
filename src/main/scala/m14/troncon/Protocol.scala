// Protocol.scala : types de messages echanges entre les Trains et le SectionController.

package m14.troncon

import akka.actor.typed.ActorRef

object Protocol {

  // Identifiant d'un train. Le coeur du projet en utilise exactement deux.
  sealed trait IdTrain
  case object Train1 extends IdTrain
  case object Train2 extends IdTrain

  // Messages que le SectionController peut recevoir.
  // Note Phase 2 (Axelobistro) : completer/affiner si besoin.
  sealed trait MessagePourControleur

  final case class Demande(emetteur: IdTrain, repondreA: ActorRef[MessagePourTrain]) extends MessagePourControleur
  final case class Sortie(emetteur: IdTrain) extends MessagePourControleur

  // Messages que le SectionController peut envoyer a un Train.
  sealed trait MessagePourTrain

  case object Autorisation extends MessagePourTrain
  case object Attente extends MessagePourTrain
}
