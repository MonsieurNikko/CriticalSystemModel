// Train.scala : acteur representant un train. Demande l'acces au troncon, attend, traverse, sort.

package m14.troncon

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object Train {

  import Protocol._

  // Point d'entree : le train demarre en etat "hors" (cf lexique.md section 1).
  // Il envoie immediatement une Demande au controleur et passe en attente.
  def apply(id: IdTrain, controleur: ActorRef[MessagePourControleur]): Behavior[MessagePourTrain] =
    comportementHors(id, controleur)

  // Etat "hors" : le train n'est pas sur le troncon ni en attente.
  // Des l'entree dans cet etat, il envoie une Demande au controleur.
  // Cote Petri : correspond a la transition Ti_demande (Ti_hors -> Ti_attente).
  private def comportementHors(id: IdTrain, controleur: ActorRef[MessagePourControleur]): Behavior[MessagePourTrain] =
    Behaviors.setup { context =>
      controleur ! Demande(id, context.self)
      comportementEnAttente(id, controleur)
    }

  // Etat "attente" : le train a envoye une Demande et attend la reponse du controleur.
  // Deux cas possibles :
  //   - Autorisation : le train entre sur le troncon (transition Ti_entree_autorisee).
  //   - Attente : le troncon est occupe, le train reste en attente (pas de transition Petri).
  private def comportementEnAttente(id: IdTrain, controleur: ActorRef[MessagePourControleur]): Behavior[MessagePourTrain] =
    Behaviors.receiveMessage {
      case Autorisation =>
        comportementSurTroncon(id, controleur)

      case Attente =>
        // Le controleur signale que le troncon est occupe. On reste en attente
        // sans rien faire, le controleur nous enverra Autorisation plus tard.
        Behaviors.same
    }

  // Etat "sur_troncon" : le train occupe le troncon partage.
  // Des l'entree dans cet etat, il envoie Sortie au controleur pour liberer la ressource.
  // Cote Petri : correspond a la transition Ti_sortie_liberation (Ti_sur_troncon -> Ti_hors + Troncon_libre).
  private def comportementSurTroncon(id: IdTrain, controleur: ActorRef[MessagePourControleur]): Behavior[MessagePourTrain] =
    Behaviors.setup { context =>
      controleur ! Sortie(id)
      comportementHors(id, controleur)
    }
}
