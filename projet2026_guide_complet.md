# 🎟️ Guide Complet — CriticalSystemModel (Projet 2026)
> Système de Billetterie Distribué · Akka + Scala + Réseaux de Pétri

---

## 📌 Résumé du projet

Construire et **vérifier formellement** un système de billetterie distribué.

Plusieurs acteurs communiquent pour gérer des réservations de billets. On doit garantir :
- qu'il est **impossible de vendre deux fois le même billet**
- qu'il n'y a **jamais de blocage (deadlock)**
- que les **règles métier sont toujours respectées** (stock ≥ 0, paiement refusé = annulation)

Deux approches en parallèle :
- **Akka + Scala** → le vrai code qui simule le système
- **Réseaux de Pétri** → le modèle mathématique qui prouve que le système est correct

---

## 🎯 Les 5 objectifs du projet

| # | Objectif | Ce qu'il faut faire |
|---|----------|---------------------|
| 1 | **État de l'art** | Étudier la vérification formelle et les réseaux de Pétri (sans outil logiciel) |
| 2 | **Modélisation** | Définir l'architecture en acteurs Akka pour la billetterie |
| 3 | **Traduction formelle** | Construire le réseau de Pétri correspondant |
| 4 | **Vérification** | Prouver : pas de deadlock, pas de double-réservation, stock jamais négatif |
| 5 | **Simulation & comparaison** | Tester le code Scala et comparer avec le modèle formel |

---

## 📦 Livrables attendus (ce qui est noté)

- [ ] **Bibliographie commentée** — sources théoriques (vérification formelle, réseaux de Pétri, LTL)
- [ ] **Code Akka/Scala fonctionnel** — simulation du système distribué
- [ ] **Réseau de Pétri** — modélisation des comportements critiques
- [ ] **Rapport de vérification** — propriétés structurelles + invariants métier
- [ ] **Comparaison simulation vs modèle formel** — comportement réel vs modèle
- [ ] **Dépôt GitHub propre et documenté**

---

## 🗓️ Plan de travail recommandé

### Semaine 1-2 — Bases et environnement

- Installer Java 11+, Scala via Coursier, sbt, extension VSCode "Scala (Metals)"
- Lire l'intro Akka : https://akka.io/docs/
- Lire le Tour of Scala : https://docs.scala-lang.org/tour/tour-of-scala.html
- Créer la structure du projet (voir ci-dessous)
- Comprendre le modèle acteur (acteur = entité indépendante qui reçoit des messages)

### Semaine 2-4 — Modélisation et code

- Coder les acteurs Akka (voir section code ci-dessous)
- Dessiner le réseau de Pétri (à la main ou sur papier — les outils logiciels sont interdits)
- Identifier les flux de messages critiques

### Semaine 4-6 — Vérification et rapport

- Analyser les propriétés structurelles du réseau de Pétri (vivacité, bornitude, deadlock)
- Formaliser les propriétés en LTL (logique temporelle)
- Rédiger le rapport de vérification
- Comparer simulation Scala et modèle formel

---

## 🗂️ Structure du projet

```
CriticalSystemModel/
├── src/
│   ├── main/
│   │   └── scala/
│   │       └── billetterie/
│   │           ├── Messages.scala           ← protocole de communication
│   │           ├── StockActor.scala         ← gère le stock de billets
│   │           ├── PaiementActor.scala      ← traite les paiements
│   │           ├── ConfirmationActor.scala  ← envoie les confirmations
│   │           ├── BilleterieActor.scala    ← orchestrateur central
│   │           ├── ClientActor.scala        ← simule un client
│   │           └── Main.scala              ← point d'entrée / simulation
│   └── test/
│       └── scala/
│           └── billetterie/
│               └── BilleterieSpec.scala    ← tests unitaires
├── petri/                                  ← schémas et analyses du réseau de Pétri
├── docs/                                   ← rapport, bibliographie
├── README.md
└── build.sbt
```

---

## ⚙️ Configuration (build.sbt)

```scala
ThisBuild / scalaVersion := "2.13.12"
ThisBuild / version      := "0.1.0"

lazy val root = (project in file("."))
  .settings(
    name := "CriticalSystemModel",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed"         % "2.8.5",
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % "2.8.5" % Test,
      "org.scalatest"     %% "scalatest"                % "3.2.17" % Test
    )
  )
```

---

## 🏗️ Architecture des acteurs

```
[ClientActor]
     │
     │  DemandeReservation
     ▼
[BilleterieActor]  ←── orchestrateur central
     │
     ├──► [StockActor]        VerifierStock / AnnulerReservation / ConfirmerReservation
     │         │
     │    StockDisponible / StockInsuffisant
     │         │
     ▼         ▼
[BilleterieActor]
     │
     ├──► [PaiementActor]     TraiterPaiement
     │         │
     │    PaiementAccepte / PaiementRefuse
     │         │
     ▼         ▼
[BilleterieActor]
     │
     ├──► [ConfirmationActor] EnvoyerConfirmation / EnvoyerAnnulation
     │
     └──► [ClientActor]       ReservationConfirmee / ReservationEchouee
```

**Invariants critiques garantis par cette architecture :**
- Le stock est toujours bloqué provisoirement avant le paiement → impossible de sur-vendre
- Si le paiement échoue, le stock est toujours restitué → stock jamais négatif
- Un seul `BilleterieActor` orchestre tout → pas de concurrence non contrôlée

---

## 💻 Code complet des acteurs

### `Messages.scala` — Le protocole de communication

```scala
package billetterie

import akka.actor.typed.ActorRef

// ─── Messages entrants vers BilleterieActor ──────────────────────────────────
sealed trait BilleterieCommand

case class DemandeReservation(
  clientId: String,
  eventId: String,
  quantite: Int,
  replyTo: ActorRef[ReponseClient]
) extends BilleterieCommand

private[billetterie] case class StockDisponible(
  clientId: String,
  eventId: String,
  quantite: Int,
  reservationId: String
) extends BilleterieCommand

private[billetterie] case class StockInsuffisant(
  clientId: String,
  raison: String,
  replyTo: ActorRef[ReponseClient]
) extends BilleterieCommand

private[billetterie] case class PaiementAccepte(
  reservationId: String,
  clientId: String
) extends BilleterieCommand

private[billetterie] case class PaiementRefuse(
  reservationId: String,
  clientId: String,
  raison: String
) extends BilleterieCommand

// ─── Messages vers StockActor ─────────────────────────────────────────────────
sealed trait StockCommand

case class VerifierStock(
  clientId: String,
  eventId: String,
  quantite: Int,
  reservationId: String,
  replyTo: ActorRef[BilleterieCommand],
  clientRef: ActorRef[ReponseClient]
) extends StockCommand

case class AnnulerReservationStock(
  reservationId: String,
  eventId: String,
  quantite: Int
) extends StockCommand

case class ConfirmerReservationStock(reservationId: String) extends StockCommand

// ─── Messages vers PaiementActor ─────────────────────────────────────────────
sealed trait PaiementCommand

case class TraiterPaiement(
  reservationId: String,
  clientId: String,
  montant: Double,
  replyTo: ActorRef[BilleterieCommand]
) extends PaiementCommand

// ─── Messages vers ConfirmationActor ─────────────────────────────────────────
sealed trait ConfirmationCommand

case class EnvoyerConfirmation(
  reservationId: String,
  clientId: String,
  eventId: String,
  quantite: Int
) extends ConfirmationCommand

case class EnvoyerAnnulation(
  reservationId: String,
  clientId: String,
  raison: String
) extends ConfirmationCommand

// ─── Réponses vers le Client ──────────────────────────────────────────────────
sealed trait ReponseClient

case class ReservationConfirmee(reservationId: String, eventId: String) extends ReponseClient
case class ReservationEchouee(clientId: String, raison: String)         extends ReponseClient
```

---

### `StockActor.scala` — Gère le stock (invariant : stock ≥ 0)

```scala
package billetterie

import akka.actor.typed.{Behavior, ActorRef}
import akka.actor.typed.scaladsl.{Behaviors, ActorContext}

object StockActor {

  case class State(
    stocks: Map[String, Int],
    reservationsProvisoires: Map[String, (String, Int, ActorRef[ReponseClient])]
    // reservationId → (eventId, quantite, clientRef)
  )

  def apply(stocksInitiaux: Map[String, Int]): Behavior[StockCommand] =
    Behaviors.setup { ctx =>
      ctx.log.info(s"[Stock] Démarrage avec stocks : $stocksInitiaux")
      active(State(stocksInitiaux, Map.empty), ctx)
    }

  private def active(state: State, ctx: ActorContext[StockCommand]): Behavior[StockCommand] =
    Behaviors.receiveMessage {

      case VerifierStock(clientId, eventId, quantite, reservationId, replyTo, clientRef) =>
        val dispo = state.stocks.getOrElse(eventId, 0)
        ctx.log.info(s"[Stock] Vérification event=$eventId demande=$quantite dispo=$dispo")

        if (dispo >= quantite) {
          val nouveauStock = state.stocks.updated(eventId, dispo - quantite)
          val nouvResa     = state.reservationsProvisoires + (reservationId -> (eventId, quantite, clientRef))
          ctx.log.info(s"[Stock] ✅ Résa provisoire $reservationId : $quantite place(s) bloquée(s)")
          replyTo ! StockDisponible(clientId, eventId, quantite, reservationId)
          active(state.copy(stocks = nouveauStock, reservationsProvisoires = nouvResa), ctx)
        } else {
          ctx.log.warn(s"[Stock] ❌ Stock insuffisant pour $eventId : $dispo dispo, $quantite demandés")
          replyTo ! StockInsuffisant(clientId, s"Stock insuffisant pour $eventId ($dispo restant(s))", clientRef)
          Behaviors.same
        }

      case AnnulerReservationStock(reservationId, eventId, quantite) =>
        val stockActuel  = state.stocks.getOrElse(eventId, 0)
        val nouveauStock = state.stocks.updated(eventId, stockActuel + quantite)
        val nouvResa     = state.reservationsProvisoires - reservationId
        ctx.log.info(s"[Stock] ↩️  Annulation $reservationId : $quantite place(s) restituée(s)")
        active(state.copy(stocks = nouveauStock, reservationsProvisoires = nouvResa), ctx)

      case ConfirmerReservationStock(reservationId) =>
        val nouvResa = state.reservationsProvisoires - reservationId
        ctx.log.info(s"[Stock] ✔️  Confirmation définitive de $reservationId")
        active(state.copy(reservationsProvisoires = nouvResa), ctx)
    }
}
```

---

### `PaiementActor.scala` — Traite les paiements

```scala
package billetterie

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import scala.util.Random

object PaiementActor {

  def apply(): Behavior[PaiementCommand] =
    Behaviors.setup { ctx =>
      ctx.log.info("[Paiement] Démarrage du service de paiement")
      Behaviors.receiveMessage {
        case TraiterPaiement(reservationId, clientId, montant, replyTo) =>
          ctx.log.info(s"[Paiement] Traitement $reservationId pour $clientId (${montant}€)")
          // Simulation : 85% succès, 15% échec
          if (Random.nextDouble() > 0.15) {
            ctx.log.info(s"[Paiement] ✅ Paiement accepté pour $reservationId")
            replyTo ! PaiementAccepte(reservationId, clientId)
          } else {
            ctx.log.warn(s"[Paiement] ❌ Paiement refusé pour $reservationId")
            replyTo ! PaiementRefuse(reservationId, clientId, "Refusé par la banque")
          }
          Behaviors.same
      }
    }
}
```

---

### `ConfirmationActor.scala` — Notifications finales

```scala
package billetterie

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object ConfirmationActor {

  def apply(): Behavior[ConfirmationCommand] =
    Behaviors.setup { ctx =>
      ctx.log.info("[Confirmation] Démarrage du service de confirmation")
      Behaviors.receiveMessage {
        case EnvoyerConfirmation(reservationId, clientId, eventId, quantite) =>
          ctx.log.info(
            s"[Confirmation] 🎟️  Email → $clientId | Résa $reservationId : $quantite billet(s) pour $eventId"
          )
          Behaviors.same

        case EnvoyerAnnulation(reservationId, clientId, raison) =>
          ctx.log.info(
            s"[Confirmation] 📧 Annulation → $clientId | Résa $reservationId ($raison)"
          )
          Behaviors.same
      }
    }
}
```

---

### `BilleterieActor.scala` — L'orchestrateur central ⭐

```scala
package billetterie

import akka.actor.typed.{Behavior, ActorRef}
import akka.actor.typed.scaladsl.{Behaviors, ActorContext}
import java.util.UUID

object BilleterieActor {

  case class ReservationEnCours(
    clientId: String,
    eventId: String,
    quantite: Int,
    montant: Double,
    replyTo: ActorRef[ReponseClient]
  )

  case class State(reservations: Map[String, ReservationEnCours])

  def apply(
    stockActor: ActorRef[StockCommand],
    paiementActor: ActorRef[PaiementCommand],
    confirmationActor: ActorRef[ConfirmationCommand]
  ): Behavior[BilleterieCommand] =
    Behaviors.setup { ctx =>
      ctx.log.info("[Billetterie] Démarrage du système de billetterie")
      active(State(Map.empty), stockActor, paiementActor, confirmationActor, ctx)
    }

  private def active(
    state: State,
    stock: ActorRef[StockCommand],
    paiement: ActorRef[PaiementCommand],
    confirmation: ActorRef[ConfirmationCommand],
    ctx: ActorContext[BilleterieCommand]
  ): Behavior[BilleterieCommand] =
    Behaviors.receiveMessage {

      // Étape 1 : demande client → vérification du stock
      case DemandeReservation(clientId, eventId, quantite, replyTo) =>
        val reservationId = UUID.randomUUID().toString.take(8).toUpperCase
        val montant       = quantite * 49.99
        ctx.log.info(s"[Billetterie] Nouvelle demande : $clientId, $eventId, qte=$quantite → id=$reservationId")
        val enCours  = ReservationEnCours(clientId, eventId, quantite, montant, replyTo)
        val newState = state.copy(reservations = state.reservations + (reservationId -> enCours))
        stock ! VerifierStock(clientId, eventId, quantite, reservationId, ctx.self, replyTo)
        active(newState, stock, paiement, confirmation, ctx)

      // Étape 2a : stock OK → lancer le paiement
      case StockDisponible(clientId, eventId, quantite, reservationId) =>
        state.reservations.get(reservationId) match {
          case Some(enCours) =>
            ctx.log.info(s"[Billetterie] Stock OK $reservationId → Paiement (${enCours.montant}€)")
            paiement ! TraiterPaiement(reservationId, clientId, enCours.montant, ctx.self)
          case None =>
            ctx.log.error(s"[Billetterie] ⚠️  Réservation inconnue : $reservationId")
        }
        Behaviors.same

      // Étape 2b : stock insuffisant → échec immédiat
      case StockInsuffisant(clientId, raison, replyTo) =>
        ctx.log.warn(s"[Billetterie] Stock insuffisant pour $clientId : $raison")
        replyTo ! ReservationEchouee(clientId, raison)
        Behaviors.same

      // Étape 3a : paiement OK → confirmation finale
      case PaiementAccepte(reservationId, clientId) =>
        state.reservations.get(reservationId) match {
          case Some(enCours) =>
            ctx.log.info(s"[Billetterie] Paiement OK $reservationId → Confirmation")
            stock       ! ConfirmerReservationStock(reservationId)
            confirmation ! EnvoyerConfirmation(reservationId, clientId, enCours.eventId, enCours.quantite)
            enCours.replyTo ! ReservationConfirmee(reservationId, enCours.eventId)
            active(state.copy(reservations = state.reservations - reservationId), stock, paiement, confirmation, ctx)
          case None =>
            ctx.log.error(s"[Billetterie] ⚠️  Réservation inconnue après paiement : $reservationId")
            Behaviors.same
        }

      // Étape 3b : paiement refusé → annulation + restitution stock
      case PaiementRefuse(reservationId, clientId, raison) =>
        state.reservations.get(reservationId) match {
          case Some(enCours) =>
            ctx.log.warn(s"[Billetterie] Paiement refusé $reservationId → Annulation")
            stock       ! AnnulerReservationStock(reservationId, enCours.eventId, enCours.quantite)
            confirmation ! EnvoyerAnnulation(reservationId, clientId, raison)
            enCours.replyTo ! ReservationEchouee(clientId, raison)
            active(state.copy(reservations = state.reservations - reservationId), stock, paiement, confirmation, ctx)
          case None =>
            ctx.log.error(s"[Billetterie] ⚠️  Réservation inconnue après refus : $reservationId")
            Behaviors.same
        }
    }
}
```

---

### `ClientActor.scala` — Simule un client

```scala
package billetterie

import akka.actor.typed.{Behavior, ActorRef}
import akka.actor.typed.scaladsl.Behaviors

object ClientActor {

  def apply(
    clientId: String,
    billetterie: ActorRef[BilleterieCommand]
  ): Behavior[ReponseClient] =
    Behaviors.setup { ctx =>
      ctx.log.info(s"[Client $clientId] En attente de réponse...")
      Behaviors.receiveMessage {
        case ReservationConfirmee(reservationId, eventId) =>
          ctx.log.info(s"[Client $clientId] 🎉 Confirmée ! ID=$reservationId, Event=$eventId")
          Behaviors.stopped

        case ReservationEchouee(_, raison) =>
          ctx.log.warn(s"[Client $clientId] 😞 Échouée : $raison")
          Behaviors.stopped
      }
    }
}
```

---

### `Main.scala` — Simulation complète

```scala
package billetterie

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors

object Main extends App {

  val system = ActorSystem(
    Behaviors.setup[Nothing] { ctx =>

      // Création des acteurs
      val confirmationActor = ctx.spawn(ConfirmationActor(), "confirmation")
      val paiementActor     = ctx.spawn(PaiementActor(), "paiement")
      val stockActor        = ctx.spawn(
        StockActor(Map(
          "concert-paris-2026" -> 100,
          "festival-lyon-2026" -> 50,
          "match-om-psg"       -> 10   // stock serré pour tester l'invariant
        )),
        "stock"
      )
      val billeterieActor = ctx.spawn(
        BilleterieActor(stockActor, paiementActor, confirmationActor),
        "billetterie"
      )

      // 5 clients concurrents
      val clients = List(
        ("alice",   "concert-paris-2026", 2),
        ("bob",     "festival-lyon-2026", 1),
        ("charlie", "match-om-psg",       5),
        ("diana",   "match-om-psg",       8), // doit échouer si Charlie passe
        ("eve",     "concert-paris-2026", 3)
      )

      clients.foreach { case (id, event, qte) =>
        val clientActor = ctx.spawn(ClientActor(id, billeterieActor), s"client-$id")
        billeterieActor ! DemandeReservation(id, event, qte, clientActor)
      }

      Behaviors.empty
    },
    "CriticalSystemModel"
  )

  Thread.sleep(5000)
  system.terminate()
}
```

---

## 🚀 Lancer le projet

```bash
# 1. Créer la structure
mkdir -p src/main/scala/billetterie src/test/scala/billetterie petri docs

# 2. Mettre les fichiers Scala dans src/main/scala/billetterie/

# 3. Compiler
sbt compile

# 4. Lancer la simulation
sbt run
```

---

## 🔵 Réseaux de Pétri — Ce qu'il faut faire (⚠️ sans outil logiciel)

### Rappel : qu'est-ce qu'un réseau de Pétri ?

Un réseau de Pétri est un graphe biparti composé de :
- **Places** (ronds) : représentent des états ou conditions
- **Transitions** (rectangles) : représentent des événements ou actions
- **Arcs** : relient places et transitions
- **Jetons** (points dans les places) : représentent des ressources ou l'état actuel

Une transition est **franchissable** si toutes ses places d'entrée ont au moins un jeton.

### Correspondance acteur ↔ réseau de Pétri

| Acteur Scala | Représentation dans le réseau de Pétri |
|---|---|
| `DemandeReservation` | Transition `T_demande` |
| `VerifierStock` | Transition `T_verif_stock` |
| `StockDisponible` | Place `P_stock_ok` |
| `StockInsuffisant` | Place `P_stock_ko` |
| `TraiterPaiement` | Transition `T_paiement` |
| `PaiementAccepte` | Place `P_paiement_ok` |
| `PaiementRefuse` | Place `P_paiement_ko` |
| `EnvoyerConfirmation` | Transition `T_confirmation` |
| `AnnulerReservationStock` | Transition `T_annulation` |

### Places principales à modéliser

```
P_idle          : client en attente
P_demande       : demande soumise
P_verif_stock   : vérification du stock en cours
P_stock_ok      : stock disponible confirmé
P_stock_ko      : stock insuffisant
P_paiement      : paiement en cours
P_paiement_ok   : paiement accepté
P_paiement_ko   : paiement refusé
P_confirme      : réservation confirmée
P_annule        : réservation annulée
P_stock[event]  : stock de billets pour un événement (N jetons = N billets)
```

### Propriétés à prouver (sans outil, à la main)

**1. Bornitude (Safety)**
Montrer que la place `P_stock[event]` ne peut jamais descendre en dessous de 0. Cela se prouve par invariant de place :

```
∀ marquage M atteignable : M(P_stock[event]) ≥ 0
```

**2. Absence de deadlock (Liveness)**
Montrer qu'il existe toujours au moins une transition franchissable à partir de tout marquage atteignable. Construire l'arbre de couverture (coverability tree).

**3. Pas de double-réservation**
Invariant : pour chaque réservation ID, la place `P_confirme` ne peut recevoir qu'un seul jeton. Cela se prouve par l'unicité des jetons identifiés.

**4. Invariants de place (P-invariants)**
Trouver un vecteur `y` tel que `y^T · C = 0` où `C` est la matrice d'incidence. Cela garantit des quantités constantes dans le système.

---

## 📐 Logique LTL — Formaliser les propriétés

La LTL (Linear Temporal Logic) permet d'exprimer des propriétés sur l'évolution dans le temps.

### Opérateurs principaux

| Opérateur | Notation | Signification |
|-----------|----------|---------------|
| Toujours | `G φ` | φ est vrai dans tous les états futurs |
| Finalement | `F φ` | φ finit par devenir vrai |
| Jusqu'à | `φ U ψ` | φ est vrai jusqu'à ce que ψ le soit |
| Suivant | `X φ` | φ est vrai à l'état suivant |

### Propriétés du système exprimées en LTL

```
// Sûreté : le stock ne devient jamais négatif
G (stock[event] ≥ 0)

// Sûreté : un billet ne peut pas être vendu deux fois
G ¬(confirme(id) ∧ confirme(id) avec id identique dans deux chemins parallèles)

// Vivacité : toute demande finit par être traitée (confirmée ou annulée)
G (demande(c) → F (confirme(c) ∨ annule(c)))

// Vivacité : un paiement refusé entraîne toujours une annulation
G (paiement_refuse(id) → X annule(id))

// Sûreté : le stock est toujours restitué si le paiement échoue
G (paiement_refuse(id) ∧ stock_bloque(id, n) → F stock_restitue(id, n))
```

---

## 👥 Répartition des rôles suggérée

| Rôle | Tâches principales |
|------|-------------------|
| **Lead Dev Scala/Akka** | Architecture acteurs, `BilleterieActor`, `StockActor`, `Main.scala` |
| **Modélisateur Pétri** | Construction réseau de Pétri, arbre de couverture, P-invariants |
| **Rédacteur Rapport** | État de l'art, bibliographie, formalisation LTL |
| **Intégration & Tests** | `BilleterieSpec.scala`, comparaison simulation vs modèle |

> ⚠️ Ces rôles ne sont pas des silos. Tout le monde doit comprendre l'ensemble.

---

## 🌿 Workflow Git

```bash
# Ne jamais travailler directement sur main
git checkout -b feature/nom-de-ma-tache

# Exemples de branches
git checkout -b feature/acteur-billetterie
git checkout -b feature/reseau-petri-reservation
git checkout -b feature/rapport-etat-de-lart
git checkout -b feature/tests-unitaires

# Conventions de commit
feat:     # Nouvelle fonctionnalité
fix:      # Correction d'un bug
docs:     # Documentation
test:     # Tests
refactor: # Restructuration sans changer le comportement

# Exemple
git commit -m "feat: ajout de StockActor avec gestion provisoire des réservations"

# Quand c'est fini : Pull Request → relecture → merge
```

---

## 📚 Bibliographie de départ

- **Murata, T. (1989).** *Petri nets: Properties, analysis and applications.* Proceedings of the IEEE — référence incontournable sur les réseaux de Pétri
- **Documentation officielle Akka :** https://akka.io/docs/
- **Tour of Scala :** https://docs.scala-lang.org/tour/tour-of-scala.html
- **Introduction LTL — TU Munich :** https://www7.in.tum.de/um/courses/verification/SS05/LTL.pdf
- **Clarke, Grumberg, Peled (1999).** *Model Checking.* MIT Press — référence sur la vérification formelle

---

*Projet réalisé dans le cadre du cours — deadline fin mai 2026*
