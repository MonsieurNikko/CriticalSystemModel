// SectionController.scala : arbitre l'acces au troncon partage entre Train1 et Train2.

package m14.troncon

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import scala.collection.immutable.Queue

object SectionController {

  import Protocol._

  // Un train en attente est memorise avec son identite et son adresse de reponse.
  // Cette adresse permet au controleur d'envoyer Autorisation plus tard, quand son tour viendra.
  private type EnAttente = (IdTrain, ActorRef[MessagePourTrain])

  // Point d'entree : a la creation, le troncon est libre (cf marquage initial M0 dans petri-troncon.md).
  def apply(): Behavior[MessagePourControleur] = etatLibre()

  // Behavior representant l'etat "EtatLibre" du controleur (cf lexique.md section 2).
  // Aucun train sur le troncon, aucun en attente.
  private def etatLibre(): Behavior[MessagePourControleur] = Behaviors.receiveMessage {

    case Demande(emetteur, repondreA) =>
      // Troncon libre : on autorise immediatement le demandeur (transition T1_entree_autorisee
      // ou T2_entree_autorisee selon emetteur, depuis un marquage avec Troncon_libre=1).
      repondreA ! Autorisation
      etatOccupe(emetteur, Queue.empty)

    case Sortie(_) =>
      // Cas defensif (cf protocole-coordination Q3) : aucun train n'occupe le troncon,
      // donc une notification de sortie est impossible si la machine d'etat est correcte.
      // On ignore sans modifier l'etat.
      Behaviors.same
  }

  // Behavior representant l'etat "EtatOccupe(occupant, fileAttente)" du controleur (cf lexique.md section 2).
  // Le train "occupant" est sur le troncon. La file FIFO contient les autres trains en attente d'autorisation.
  private def etatOccupe(occupant: IdTrain, fileAttente: Queue[EnAttente]): Behavior[MessagePourControleur] =
    Behaviors.receiveMessage {

      case Demande(emetteur, _) if emetteur == occupant =>
        // Cas defensif (cf Q2) : un train sur le troncon ne devrait pas re-demander.
        // On ignore : ne pas le mettre en file (il est deja occupant), ne pas lui repondre.
        Behaviors.same

      case Demande(emetteur, repondreA) =>
        // Le troncon est pris : on enfile et on signale au demandeur d'attendre.
        // Note : la transition Petri Ti_demande s'est deja produite cote train (passage hors -> attente).
        // L'envoi de Attente cote Akka est une notification de protocole, sans correspondant Petri
        // (cf petri-troncon.md section 8 et comparaison.md scenario 2).
        repondreA ! Attente
        etatOccupe(occupant, fileAttente.enqueue((emetteur, repondreA)))

      case Sortie(emetteur) if emetteur == occupant =>
        // L'occupant libere : on regarde s'il y a un train en attente a promouvoir.
        promouvoirOuLiberer(fileAttente)

      case Sortie(_) =>
        // Cas defensif : un train qui n'est pas l'occupant ne devrait pas envoyer Sortie.
        Behaviors.same
    }

  // Helper extrait pour garder etatOccupe sous le seuil des 30 lignes (cf REGLES_PROJET section 12).
  // Decide du Behavior suivant apres une sortie de l'occupant : libre ou nouvel occupant promu.
  private def promouvoirOuLiberer(fileAttente: Queue[EnAttente]): Behavior[MessagePourControleur] = {
    fileAttente.dequeueOption match {
      case None =>
        // Personne en attente : transition Ti_sortie_liberation, retour a EtatLibre.
        etatLibre()
      case Some(((prochainTrain, prochainRepondreA), reste)) =>
        // Train en tete de file : on lui envoie Autorisation et il devient le nouvel occupant.
        // Cote Petri c'est l'enchainement Ti_sortie_liberation puis Tj_entree_autorisee.
        prochainRepondreA ! Autorisation
        etatOccupe(prochainTrain, reste)
    }
  }
}
