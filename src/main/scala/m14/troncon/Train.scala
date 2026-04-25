// Train.scala : acteur representant un train. Demande l'acces au troncon, attend, sort.

package m14.troncon

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object Train {

  import Protocol._

  // Squelette Phase 1 : compile mais ne fait rien.
  // Phase 3 (Nikko) : machine a etats hors -> attente -> sur_troncon -> hors.
  def apply(id: IdTrain, controleur: ActorRef[MessagePourControleur]): Behavior[MessagePourTrain] = Behaviors.receiveMessage { _ =>
    Behaviors.same
  }
}
