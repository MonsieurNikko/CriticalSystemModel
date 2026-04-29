// Train.scala : acteur representant un train. Cycle complet etendu PSD :
//   hors -> attente (canton) -> sur_canton -> a_quai (avec cycle portes) -> hors -> ...
//
// 4 etats (cf petri-troncon.md section 2 et lexique.md section 1) :
//   - comportementHors        : le train est hors zone (en amont ou apres avoir quitte le quai)
//   - comportementEnAttente   : le train a demande l'acces au canton et attend reponse
//   - comportementSurCanton   : le train occupe le canton, demande l'acces au quai
//   - comportementAQuai       : le train est a l'arret a quai, fait son cycle de portes
//
// Mapping vers les transitions Petri :
//   hors -> attente             : T1_demande / T2_demande
//   attente -> sur_canton       : T1_entree_canton / T2_entree_canton  (via Autorisation du SectionController)
//   sur_canton -> a_quai        : T1_arrivee_quai / T2_arrivee_quai     (via Autorisation du QuaiController + Sortie au SectionController)
//   a_quai (cycle portes)        : Ouverture_portes_Ti puis Fermeture_portes_Ti
//   a_quai -> hors              : T1_depart_quai / T2_depart_quai      (via DepartQuai au QuaiController)

package m14.troncon

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object Train {

  import Protocol._

  // Point d'entree : le train demarre en etat "hors" (cf lexique.md section 1).
  def apply(
    id: IdTrain,
    sectionController: ActorRef[MessagePourControleur],
    quaiController: ActorRef[MessagePourQuai],
    gestionnairePortes: ActorRef[MessagePourPortes]
  ): Behavior[MessagePourTrain] =
    comportementHors(id, sectionController, quaiController, gestionnairePortes)

  // Etat "hors" : le train n'est ni sur le canton ni a quai.
  // Des l'entree dans cet etat, il envoie une Demande au SectionController et passe en attente.
  // Cote Petri : transition Ti_demande (Ti_hors -> Ti_attente).
  private def comportementHors(
    id: IdTrain,
    sc: ActorRef[MessagePourControleur],
    qc: ActorRef[MessagePourQuai],
    gp: ActorRef[MessagePourPortes]
  ): Behavior[MessagePourTrain] =
    Behaviors.setup { context =>
      sc ! Demande(id, context.self)
      comportementEnAttente(id, sc, qc, gp)
    }

  // Etat "attente" : le train a envoye une Demande au SectionController et attend la reponse.
  //   - Autorisation : le train entre sur le canton (transition Ti_entree_canton).
  //   - Attente      : le canton est occupe, le train reste en attente.
  private def comportementEnAttente(
    id: IdTrain,
    sc: ActorRef[MessagePourControleur],
    qc: ActorRef[MessagePourQuai],
    gp: ActorRef[MessagePourPortes]
  ): Behavior[MessagePourTrain] =
    Behaviors.receiveMessage {
      case Autorisation =>
        comportementSurCanton(id, sc, qc, gp)

      case Attente =>
        // Le SectionController nous notifie que le canton est occupe.
        // On reste en attente passive : il enverra Autorisation plus tard.
        Behaviors.same

      case PortesOuvertes | PortesFermees =>
        // Cas defensif : on ne devrait pas recevoir d'acquittement portes en attente canton.
        Behaviors.same
    }

  // Etat "sur_canton" : le train occupe le canton.
  // Des l'entree dans cet etat, il envoie ArriveeQuai au QuaiController.
  //   - Autorisation : le quai est libre. Le train libere le canton (Sortie au SC) et passe a quai.
  //   - Attente      : le quai est occupe. Le train reste sur le canton (back-off, sera promu).
  private def comportementSurCanton(
    id: IdTrain,
    sc: ActorRef[MessagePourControleur],
    qc: ActorRef[MessagePourQuai],
    gp: ActorRef[MessagePourPortes]
  ): Behavior[MessagePourTrain] =
    Behaviors.setup { context =>
      qc ! ArriveeQuai(id, context.self)
      Behaviors.receiveMessage {
        case Autorisation =>
          // Quai libre : on libere le canton et on entre a quai (transition Ti_arrivee_quai).
          sc ! Sortie(id)
          comportementAQuai(id, sc, qc, gp)

        case Attente =>
          // Quai occupe : on reste sur le canton, le QuaiController nous repondra plus tard.
          Behaviors.same

        case PortesOuvertes | PortesFermees =>
          // Cas defensif : on ne devrait pas recevoir d'acquittement portes sur canton.
          Behaviors.same
      }
    }

  // Etat "a_quai" : le train est a l'arret en station.
  // Cycle complet : OuverturePortes -> PortesOuvertes -> FermeturePortes -> PortesFermees -> DepartQuai.
  // Cote Petri : Ouverture_portes_Ti puis Fermeture_portes_Ti puis Ti_depart_quai.
  private def comportementAQuai(
    id: IdTrain,
    sc: ActorRef[MessagePourControleur],
    qc: ActorRef[MessagePourQuai],
    gp: ActorRef[MessagePourPortes]
  ): Behavior[MessagePourTrain] =
    Behaviors.setup { context =>
      gp ! OuverturePortes(id, context.self)
      attendrePortesOuvertes(id, sc, qc, gp)
    }

  // Sous-etat de "a_quai" : on attend l'ack PortesOuvertes du gestionnaire.
  private def attendrePortesOuvertes(
    id: IdTrain,
    sc: ActorRef[MessagePourControleur],
    qc: ActorRef[MessagePourQuai],
    gp: ActorRef[MessagePourPortes]
  ): Behavior[MessagePourTrain] =
    Behaviors.receiveMessage {
      case PortesOuvertes =>
        Behaviors.setup { context =>
          gp ! FermeturePortes(id, context.self)
          attendrePortesFermees(id, sc, qc, gp)
        }
      case _ =>
        Behaviors.same
    }

  // Sous-etat de "a_quai" : on attend l'ack PortesFermees, puis on quitte le quai.
  private def attendrePortesFermees(
    id: IdTrain,
    sc: ActorRef[MessagePourControleur],
    qc: ActorRef[MessagePourQuai],
    gp: ActorRef[MessagePourPortes]
  ): Behavior[MessagePourTrain] =
    Behaviors.receiveMessage {
      case PortesFermees =>
        // Portes fermees : on libere le quai et on retourne hors (transition Ti_depart_quai).
        qc ! DepartQuai(id)
        comportementHors(id, sc, qc, gp)
      case _ =>
        Behaviors.same
    }
}
