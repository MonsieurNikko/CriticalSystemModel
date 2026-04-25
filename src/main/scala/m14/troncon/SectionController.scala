// SectionController.scala : arbitre l'acces au troncon partage entre Train1 et Train2.

package m14.troncon

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object SectionController {

  import Protocol._

  // Squelette Phase 1 : compile mais ne fait rien.
  // Phase 2 (Alicette) : etat libre / occupe T1 / occupe T2 + file d'attente FIFO.
  def apply(): Behavior[MessagePourControleur] = Behaviors.receiveMessage { _ =>
    Behaviors.same
  }
}
