// QuaiController.scala : arbitre l'acces au quai en station entre Train1 et Train2.
// Clone structurel de SectionController : meme pattern libre/occupe + file FIFO,
// applique cette fois aux places Petri Quai_libre / Ti_a_quai (cf petri-troncon.md).

package m14.troncon

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import scala.collection.immutable.Queue

object QuaiController {

  import Protocol._

  private type EnAttente = (IdTrain, ActorRef[MessagePourTrain])

  // Point d'entree : a la creation, le quai est libre (Quai_libre=1 dans M0).
  def apply(): Behavior[MessagePourQuai] = quaiLibre()

  // Etat "Quai libre" : aucun train n'est a quai, aucun en attente.
  private def quaiLibre(): Behavior[MessagePourQuai] = Behaviors.receiveMessage {

    case ArriveeQuai(emetteur, repondreA) =>
      // Quai libre : on autorise immediatement le demandeur (transition Ti_arrivee_quai).
      repondreA ! Autorisation
      quaiOccupe(emetteur, Queue.empty)

    case DepartQuai(_) =>
      // Cas defensif : aucun train n'est a quai, donc une notification de depart est
      // impossible si la machine d'etat est correcte. On ignore.
      Behaviors.same
  }

  // Etat "Quai occupe par occupant" + file FIFO des trains en attente d'acces au quai.
  private def quaiOccupe(occupant: IdTrain, fileAttente: Queue[EnAttente]): Behavior[MessagePourQuai] =
    Behaviors.receiveMessage {

      case ArriveeQuai(emetteur, _) if emetteur == occupant =>
        // Cas defensif : un train deja a quai ne devrait pas re-demander. Ignore.
        Behaviors.same

      case ArriveeQuai(emetteur, repondreA) =>
        // Quai pris : on enfile et on signale au demandeur d'attendre.
        // Note : le train reste sur le canton tant qu'il n'a pas recu Autorisation.
        repondreA ! Attente
        quaiOccupe(occupant, fileAttente.enqueue((emetteur, repondreA)))

      case DepartQuai(emetteur) if emetteur == occupant =>
        // L'occupant libere le quai : on regarde s'il y a un train en attente a promouvoir.
        promouvoirOuLiberer(fileAttente)

      case DepartQuai(_) =>
        // Cas defensif : un train qui n'est pas l'occupant ne devrait pas envoyer DepartQuai.
        Behaviors.same
    }

  // Helper extrait pour respecter la regle des 30 lignes (REGLES_PROJET section 12).
  private def promouvoirOuLiberer(fileAttente: Queue[EnAttente]): Behavior[MessagePourQuai] = {
    fileAttente.dequeueOption match {
      case None =>
        quaiLibre()
      case Some(((prochainTrain, prochainRepondreA), reste)) =>
        prochainRepondreA ! Autorisation
        quaiOccupe(prochainTrain, reste)
    }
  }
}
