# Rapport de verification - CriticalSystemModel (modele etendu PSD)

> Livrable L4 du cahier des charges. Rapport detaille de la verification des proprietes structurelles et des invariants metier du sous-systeme critique M14, etendu le 29/04/2026 a la gestion des portes palieres (PSD).
>
> Etat : rapport finalise le 01/05/2026 avec la sortie reelle de l'analyseur (20 marquages, 40 arcs etiquetes, 5 invariants PASSE, 0 deadlock, 5 proprietes LTL PASSE). La section 5 reprend les tableaux du carnet de preuves ; les annexes A1-A4 donnent les sorties et references de verification.

---

## 1) Contexte et motivation

### 1.1 Domaine applicatif

La ligne 14 du metro de Paris (RATP) est la premiere ligne integralement automatisee de France et la premiere equipee sur l'ensemble de ses quais de **portes palieres (PSD - Platform Screen Doors)**. L'absence de conducteur impose que toutes les fonctions de surete soient assurees par le systeme embarque et le systeme central : detection de presence des trains, arbitrage des cantons de signalisation, ouverture/fermeture des portes synchronisee avec l'arret a quai, garantie d'absence de depart avec portes ouvertes. Une defaillance dans cette boucle critique entraine selon le cas (1) une collision si deux trains se retrouvent sur le meme canton, (2) une chute mortelle d'un voyageur si les portes palieres s'ouvrent sans train present, (3) un voyageur coince ou ecrase si un train demarre alors que les portes sont encore ouvertes.

### 1.2 Sous-systeme retenu

Nous modelisons un troncon critique compose de **un canton de signalisation suivi de un quai equipe de PSD**, partage par **deux trains automatiques**. Trois controleurs centralises arbitrent l'acces : `SectionController` (canton), `QuaiController` (quai), `GestionnairePortes` (portes palieres). L'objectif est de prouver qu'aucun des trois scenarios accidentels ci-dessus n'est atteignable depuis l'etat initial.

### 1.3 Choix d'abstraction

Conformement au recadrage de cadrage `documentation/contexte/recadrage-m14-troncon-critique.md` (sections 1-3 et 13), le scope est volontairement minimaliste mais defendable : 2 trains, 1 canton, 1 quai, 1 paire de PSD, 5 acteurs Akka, 12 places et 12 transitions Petri. L'extension PSD du 29/04/2026 (section 13 du recadrage) ajoute la dimension critique manquante (les portes palieres) sans faire exploser la combinatoire : on passe de 8 a 20 marquages atteignables, ce qui reste enumerable a la main et programmatiquement.

La doctrine du sprint, resumee dans `documentation/suivi/PLAN.md` section 8, est explicite : **simple, propre et verifiable** plutot que realiste mais inanalysable. Les extensions naturelles (N trains, second canton en serie, deux quais opposes, Petri temporise) sont mentionnees en section 8 comme pistes mais explicitement hors sprint.

### 1.4 Demarche progressive du projet

Le modele final n'a pas ete pose directement. Pour garder un projet defendable, nous avons travaille en deux temps :

| Etape | Perimetre | But de l'etape | Etat obtenu |
|---|---|---|---|
| **Socle initial** | 2 trains + 1 troncon partage | Construire et verifier le mecanisme minimal d'exclusion mutuelle : demande, attente, autorisation, sortie/liberation. | 3 acteurs Akka, 7 places Petri, 6 transitions, 8 marquages atteignables, 1 invariant principal. |
| **Upgrade M14 / PSD** | 2 trains + canton + quai + portes palieres | Ajouter la dimension station M14 : arret a quai, ouverture/fermeture PSD, depart uniquement portes fermees. | 5 acteurs Akka, 12 places, 12 transitions, 20 marquages, 5 invariants, 5 proprietes LTL bornees. |

Le terme `troncon` du socle initial a ete precise en `canton` dans le modele final. Cette evolution n'est pas un changement de sujet : le canton reste la premiere ressource critique partagee entre les deux trains ; le quai et les portes sont ajoutes ensuite comme extension de surete.

---

## 2) Bibliographie

Les 11 sources retenues sont commentees integralement dans `documentation/livrables/biblio.md`. Synthese par theme :

- **Reseaux de Petri** : Murata 1989 (proprietes structurelles, invariants P et T) ; ouvrage de reference pour les sections 4 et 5.
- **Modele acteur** : documentation Akka Typed (Behaviors, message protocols), Hewitt 1973 (origines theoriques).
- **Verification formelle et LTL** : Baier & Katoen *Principles of Model Checking* (2008), Manna & Pnueli *The Temporal Logic of Reactive and Concurrent Systems* (1992), cours LTL TU Munich. Bases pour la formalisation section 5.8.
- **Concurrence** : Magee & Kramer *Concurrency: State Models and Java Programs* (exclusion mutuelle, deadlock), Lamport (logical clocks, fairness).
- **Domaine ferroviaire critique** : documentation publique RATP M14, IEEE Std 1474 CBTC (Communications-Based Train Control), etudes UITP sur les portes palieres. Sources ajoutees lors de l'extension PSD pour justifier le caractere reglementaire des invariants 5.5.1 et 5.5.2.

Chaque source est referencee au moins une fois dans les sections 1, 4, 5 ou 8 du present rapport.

---

## 3) Architecture Akka

### 3.1 Diagramme des acteurs (5 acteurs)

```
  +---------+   Demande / Sortie         +---------------------+
  | Train1  |--------------------------->|  SectionController  |
  +---------+   Autorisation / Attente   |   (canton)          |
      |        <---------------------------|                     |
      |                                  +---------------------+
      | ArriveeQuai / DepartQuai
      v
  +---------------------+    Autorisation / Attente
  |   QuaiController    |---------------------------> Train1, Train2
  |   (quai)            |
  +---------------------+
      ^
      | OuverturePortes / FermeturePortes
      |
  +---------------------+    PortesOuvertes / PortesFermees
  |  GestionnairePortes |---------------------------> Train1, Train2
  |  (PSD)              |
  +---------------------+
```

Cinq acteurs au total (vs 3 dans le modele initial) : `Train1`, `Train2`, `SectionController`, `QuaiController`, `GestionnairePortes`. Tous sont des `Behavior[T]` typés (Akka Typed, Scala 2.13).

### 3.2 Machine d'etats du Train (4 etats)

Chaque `Train` est implemente comme une cascade de quatre `Behavior` (cf `src/main/scala/m14/troncon/Train.scala`) :

1. `comportementHors` : etat initial. Le train envoie `Demande` au `SectionController` en sortie d'init et passe en attente.
2. `comportementEnAttente` : reception de `Autorisation` -> passage en `comportementSurCanton` ; reception de `Attente` -> reste dans cet etat.
3. `comportementSurCanton` : envoie `ArriveeQuai` au `QuaiController`. Apres reception de `Autorisation` du quai, le train envoie `Sortie` au `SectionController` puis passe en `comportementAQuai`.
4. `comportementAQuai` : envoie `OuverturePortes` au `GestionnairePortes`, attend `PortesOuvertes`, envoie `FermeturePortes`, attend `PortesFermees`, envoie `DepartQuai` au `QuaiController`, retourne en `comportementHors` (cycle ferme).

Deux helpers (`enAttenteOuverturePortes`, `enAttenteFermeturePortes`) materialisent les ack de portes.

### 3.3 Protocole (10 types de messages)

Defini dans `src/main/scala/m14/troncon/Protocol.scala` :

- **Vers controleurs canton/quai** (6) : `Demande`, `Sortie`, `ArriveeQuai`, `DepartQuai`, `OuverturePortes`, `FermeturePortes`.
- **Vers trains** (4) : `Autorisation`, `Attente`, `PortesOuvertes`, `PortesFermees`.

Les types ADT (sealed traits `MessagePourControleur`, `MessagePourQuai`, `MessagePourPortes`, `MessagePourTrain`) garantissent qu'aucun acteur ne peut recevoir un message hors de son protocole (verification au compile).

### 3.4 Arbitrage des controleurs

`SectionController` et `QuaiController` partagent un design symetrique (cf `protocole-coordination.md` Q11) : etats `libre()` / `occupe(occupant, file: Queue[ActorRef])`, file FIFO immuable. A reception de `Demande`/`ArriveeQuai`, le controleur autorise immediatement si libre, sinon enfile et envoie `Attente`. A reception de `Sortie`/`DepartQuai`, le controleur depile le suivant et lui envoie `Autorisation` (ou repasse `libre()` si la file est vide). Une garde defensive ignore les `Sortie`/`DepartQuai` provenant d'un acteur qui n'est pas l'occupant courant.

### 3.5 Garde de surete critique du GestionnairePortes

`GestionnairePortes` (cf `src/main/scala/m14/troncon/GestionnairePortes.scala`) a deux etats : `portesFermees()` et `portesOuvertes(occupant)`. **Garde CRITIQUE** :

- En `portesFermees()`, une `OuverturePortes(emetteur)` est acceptee inconditionnellement (le `QuaiController` est suppose avoir deja autorise l'emetteur a quai). Cote tests, on verifie que rien dans le protocole ne permet d'envoyer cette ouverture sans avoir au prealable obtenu le quai.
- En `portesOuvertes(occupant)`, toute `OuverturePortes` ou `FermeturePortes` provenant d'un emetteur != occupant est **silencieusement refusee** (aucun ack envoye, etat inchange). Cette garde est testee explicitement par deux tests CRITIQUES de `GestionnairePortesSpec`.

Au niveau Akka, PSD-Open est materialise par le couple **machine d'etats du `Train`** (seul `comportementAQuai` envoie `OuverturePortes`) + garde defensive du `GestionnairePortes` contre un second occupant. Au niveau Petri, le meme invariant est materialise structurellement par le pre `{Ti_a_quai, Portes_fermees}` de `Ouverture_portes_Ti` (section 4.2).

### 3.6 Hypotheses du modele

- Pas de panne d'acteur, pas de message perdu, pas de timeout (les notifications `Attente` et les ack portes sont supposees toujours delivrees).
- Pas de duree d'arret a quai (le delai entre `OuverturePortes` et `FermeturePortes` est nul cote modele).
- Determinisme des files FIFO Akka (vs choix non deterministe cote Petri, cf section 7).

Toutes ces hypotheses sont reprises dans la section 7 "Limites assumees".

---

## 4) Modele Petri formel

### 4.1 Les 12 places

Reparties en trois blocs (cf `petri/petri-troncon.md` section 3) :

- **4 ressources globales** : `Canton_libre`, `Quai_libre`, `Portes_fermees`, `Portes_ouvertes`.
- **4 etats du Train1** : `T1_hors`, `T1_attente`, `T1_sur_canton`, `T1_a_quai`.
- **4 etats du Train2** : `T2_hors`, `T2_attente`, `T2_sur_canton`, `T2_a_quai`.

Marquage initial `M0 = (Canton_libre, Quai_libre, Portes_fermees, T1_hors, T2_hors)` : 5 jetons.

### 4.2 Les 12 transitions

Pour chaque train Ti (i in {1,2}), six transitions :

| Transition           | Pre                                  | Post                              |
|----------------------|--------------------------------------|-----------------------------------|
| `Ti_demande`         | `{Ti_hors}`                          | `{Ti_attente}`                    |
| `Ti_entree_canton`   | `{Ti_attente, Canton_libre}`         | `{Ti_sur_canton}`                 |
| `Ti_arrivee_quai`    | `{Ti_sur_canton, Quai_libre}`        | `{Ti_a_quai, Canton_libre}`       |
| `Ouverture_portes_Ti`| `{Ti_a_quai, Portes_fermees}`        | `{Ti_a_quai, Portes_ouvertes}`    |
| `Fermeture_portes_Ti`| `{Ti_a_quai, Portes_ouvertes}`       | `{Ti_a_quai, Portes_fermees}`     |
| `Ti_depart_quai`     | `{Ti_a_quai, Portes_fermees}`        | `{Ti_hors, Quai_libre, Portes_fermees}` |

Notes :

- **Read-arc emule** sur `Portes_fermees` dans `Ti_depart_quai` : la transition consomme puis reproduit le jeton `Portes_fermees`. C'est l'astuce standard pour simuler une garde sans effet de bord en Petri ordinaire (Murata 1989). Cote Akka, l'equivalent est une lecture de l'etat `portesFermees()` sans transition d'etat du `GestionnairePortes`.
- **Reproduction de `Ti_a_quai`** dans `Ouverture_portes_Ti` et `Fermeture_portes_Ti` : le train reste a quai pendant l'ouverture/fermeture des portes.

### 4.3 Pre-conditions structurelles de surete PSD

Les deux invariants critiques sont materialises **structurellement** :

- `Ouverture_portes_Ti` exige `Ti_a_quai` dans son pre. Donc `Portes_ouvertes` ne peut etre marquee que si un train Ti est a quai. Preuve par induction = invariant PSD-Open.
- `Ti_depart_quai` exige `Portes_fermees` dans son pre. Donc un depart est impossible portes ouvertes. = invariant PSD-Departure.

Ces deux gardes sont la traduction Petri exacte des deux gardes Akka du `GestionnairePortes` (section 3.5).

### 4.4 Schema ASCII

Reproduit depuis `petri/petri-troncon.md` section 4 (cf fichier source pour le diagramme complet ; non duplique ici pour eviter la divergence).

### 4.5 Pourquoi 12 places et 12 transitions, pas plus

Le scope est verrouille par `documentation/gouvernance/protocole-coordination.md` section 2 :

- 2 trains x 4 etats = 8 places etat + 4 ressources globales = 12 places.
- 2 trains x 6 transitions = 12 transitions.

Toute extension (N trains, second canton, second quai) multiplie ces nombres et fait exploser l'espace d'etats au-dela de l'enumeration a la main. Conformement a la doctrine du sprint (`PLAN.md` section 8), nous renforcons la **profondeur** de la verification (5 invariants + LTL formalisee) plutot que la complexite du modele.

---

## 5) Verification des proprietes (coeur du rapport)

### 5.1 Invariant canton

Enonce : `M(T1_sur_canton) + M(T2_sur_canton) + M(Canton_libre) = 1` pour tout marquage atteignable.

Preuve a deux niveaux : preservation structurelle par les transitions de `petri/petri-troncon.md` section 5.1, puis enumeration exhaustive par l'analyseur Scala (`verifierInvariantCanton`) sur les 20 marquages atteignables.

| Marquage | T1_sur_canton | T2_sur_canton | Canton_libre | Somme | Resultat |
|----------|--------------:|--------------:|-------------:|------:|:--------:|
| M0       | 0 | 0 | 1 | 1 | PASSE |
| M1       | 0 | 0 | 1 | 1 | PASSE |
| M2       | 0 | 0 | 1 | 1 | PASSE |
| M3       | 1 | 0 | 0 | 1 | PASSE |
| M4       | 0 | 0 | 1 | 1 | PASSE |
| M5       | 0 | 1 | 0 | 1 | PASSE |
| M6       | 0 | 0 | 1 | 1 | PASSE |
| M7       | 1 | 0 | 0 | 1 | PASSE |
| M8       | 0 | 1 | 0 | 1 | PASSE |
| M9       | 0 | 0 | 1 | 1 | PASSE |
| M10      | 0 | 0 | 1 | 1 | PASSE |
| M11      | 0 | 0 | 1 | 1 | PASSE |
| M12      | 0 | 0 | 1 | 1 | PASSE |
| M13      | 0 | 0 | 1 | 1 | PASSE |
| M14      | 0 | 1 | 0 | 1 | PASSE |
| M15      | 0 | 0 | 1 | 1 | PASSE |
| M16      | 1 | 0 | 0 | 1 | PASSE |
| M17      | 0 | 0 | 1 | 1 | PASSE |
| M18      | 0 | 1 | 0 | 1 | PASSE |
| M19      | 1 | 0 | 0 | 1 | PASSE |

**Consequence** : exclusion mutuelle stricte sur le canton de signalisation. Pas de collision possible.

### 5.2 Invariant quai

Enonce : `M(T1_a_quai) + M(T2_a_quai) + M(Quai_libre) = 1` pour tout marquage atteignable.

| Marquage | T1_a_quai | T2_a_quai | Quai_libre | Somme | Resultat |
|----------|----------:|----------:|-----------:|------:|:--------:|
| M0       | 0 | 0 | 1 | 1 | PASSE |
| M1       | 0 | 0 | 1 | 1 | PASSE |
| M2       | 0 | 0 | 1 | 1 | PASSE |
| M3       | 0 | 0 | 1 | 1 | PASSE |
| M4       | 0 | 0 | 1 | 1 | PASSE |
| M5       | 0 | 0 | 1 | 1 | PASSE |
| M6       | 1 | 0 | 0 | 1 | PASSE |
| M7       | 0 | 0 | 1 | 1 | PASSE |
| M8       | 0 | 0 | 1 | 1 | PASSE |
| M9       | 0 | 1 | 0 | 1 | PASSE |
| M10      | 1 | 0 | 0 | 1 | PASSE |
| M11      | 1 | 0 | 0 | 1 | PASSE |
| M12      | 0 | 1 | 0 | 1 | PASSE |
| M13      | 0 | 1 | 0 | 1 | PASSE |
| M14      | 1 | 0 | 0 | 1 | PASSE |
| M15      | 1 | 0 | 0 | 1 | PASSE |
| M16      | 0 | 1 | 0 | 1 | PASSE |
| M17      | 0 | 1 | 0 | 1 | PASSE |
| M18      | 1 | 0 | 0 | 1 | PASSE |
| M19      | 0 | 1 | 0 | 1 | PASSE |

Verification programmatique : `verifierInvariantQuai`.

**Consequence** : exclusion mutuelle stricte sur le quai. Pas de collision en station.

### 5.3 Invariant portes

Enonce : `M(Portes_fermees) + M(Portes_ouvertes) = 1` pour tout marquage atteignable.

| Marquage | Portes_fermees | Portes_ouvertes | Somme | Resultat |
|----------|---------------:|----------------:|------:|:--------:|
| M0..M10  | 1 | 0 | 1 | PASSE |
| M11      | 0 | 1 | 1 | PASSE |
| M12      | 1 | 0 | 1 | PASSE |
| M13      | 0 | 1 | 1 | PASSE |
| M14      | 1 | 0 | 1 | PASSE |
| M15      | 0 | 1 | 1 | PASSE |
| M16      | 1 | 0 | 1 | PASSE |
| M17      | 0 | 1 | 1 | PASSE |
| M18      | 0 | 1 | 1 | PASSE |
| M19      | 0 | 1 | 1 | PASSE |

Verification programmatique : `verifierInvariantPortes`.

**Consequence** : les portes palieres sont toujours dans exactement un etat (ouvertes ou fermees), jamais dans un etat indetermine. Propriete de coherence d'etat.

### 5.4 Invariants par train (sur 4 etats)

Enonce : `M(Ti_hors) + M(Ti_attente) + M(Ti_sur_canton) + M(Ti_a_quai) = 1` pour i in {1,2}, sur tout marquage atteignable.

Le tableau ci-dessous donne l'etat unique actif de chaque train pour chaque marquage. Chaque ligne vaut donc une somme de 1 pour T1 et une somme de 1 pour T2.

| Marquage | Etat actif T1 | Etat actif T2 | Resultat |
|----------|---------------|---------------|:--------:|
| M0       | `T1_hors`        | `T2_hors`        | PASSE |
| M1       | `T1_attente`     | `T2_hors`        | PASSE |
| M2       | `T1_hors`        | `T2_attente`     | PASSE |
| M3       | `T1_sur_canton`  | `T2_hors`        | PASSE |
| M4       | `T1_attente`     | `T2_attente`     | PASSE |
| M5       | `T1_hors`        | `T2_sur_canton`  | PASSE |
| M6       | `T1_a_quai`      | `T2_hors`        | PASSE |
| M7       | `T1_sur_canton`  | `T2_attente`     | PASSE |
| M8       | `T1_attente`     | `T2_sur_canton`  | PASSE |
| M9       | `T1_hors`        | `T2_a_quai`      | PASSE |
| M10      | `T1_a_quai`      | `T2_attente`     | PASSE |
| M11      | `T1_a_quai`      | `T2_hors`        | PASSE |
| M12      | `T1_attente`     | `T2_a_quai`      | PASSE |
| M13      | `T1_hors`        | `T2_a_quai`      | PASSE |
| M14      | `T1_a_quai`      | `T2_sur_canton`  | PASSE |
| M15      | `T1_a_quai`      | `T2_attente`     | PASSE |
| M16      | `T1_sur_canton`  | `T2_a_quai`      | PASSE |
| M17      | `T1_attente`     | `T2_a_quai`      | PASSE |
| M18      | `T1_a_quai`      | `T2_sur_canton`  | PASSE |
| M19      | `T1_sur_canton`  | `T2_a_quai`      | PASSE |

Verification programmatique : `verifierInvariantsParTrain`.

### 5.5 Surete PSD (CRITIQUE - coeur de l'extension)

#### 5.5.1 PSD-Open Safety (portes ouvertes implique train a quai)

Enonce LTL : `G ( Portes_ouvertes = 1 => T1_a_quai + T2_a_quai = 1 )`.

**Triple preuve** (cf `documentation/gouvernance/protocole-coordination.md` Q15) :

1. **Structurelle** : la place `Portes_ouvertes` ne peut etre marquee que par les transitions `Ouverture_portes_Ti`, dont le pre contient `Ti_a_quai`. Donc tant que `Portes_ouvertes=1`, un train est a quai. La preuve est inductive sur les transitions.

2. **Inductive** : par recurrence sur le marquage initial (M0 a `Portes_ouvertes=0`, donc l'implication est trivialement vraie) et chaque transition.

3. **Programmatique** : l'analyseur Scala (`verifierSurteOuverturePortes`) enumere les marquages atteignables et verifie pour chacun l'implication. Si la verification echoue sur un seul marquage, l'analyseur signale une violation.

| Marquage avec `Portes_ouvertes=1` | T1_a_quai | T2_a_quai | Train a quai | Resultat |
|-----------------------------------|----------:|----------:|--------------|:--------:|
| M11 | 1 | 0 | T1 | PASSE |
| M13 | 0 | 1 | T2 | PASSE |
| M15 | 1 | 0 | T1 | PASSE |
| M17 | 0 | 1 | T2 | PASSE |
| M18 | 1 | 0 | T1 | PASSE |
| M19 | 0 | 1 | T2 | PASSE |

**Consequence M14** : les voyageurs ne peuvent jamais tomber sur les voies via les portes palieres. **Surete reglementaire CRITIQUE**.

#### 5.5.2 PSD-Departure Safety (depart implique portes fermees)

Enonce : pour tout marquage M et tout train Ti, si `Ti_depart_quai` est tirable depuis M, alors `M(Portes_fermees)=1`.

**Justification structurelle** : c'est inscrit dans le pre de `Ti_depart_quai` : `{Ti_a_quai, Portes_fermees}`. La transition n'est pas tirable sinon. Verification programmatique : `verifierSurteDepartQuai` enumere les marquages et verifie la condition.

| Marquage | T1_depart_quai tirable | T2_depart_quai tirable | Portes_fermees | Resultat |
|----------|:----------------------:|:----------------------:|---------------:|:--------:|
| M6       | OUI | NON | 1 | PASSE |
| M9       | NON | OUI | 1 | PASSE |
| M10      | OUI | NON | 1 | PASSE |
| M12      | NON | OUI | 1 | PASSE |
| M14      | OUI | NON | 1 | PASSE |
| M16      | NON | OUI | 1 | PASSE |
| M11, M13, M15, M17, M18, M19 | NON | NON | 0 | non tirable, surete OK |
| Autres marquages | NON | NON | 1 | pas de train a quai |

**Consequence M14** : aucun voyageur ne peut etre coince ou ecrase par un train qui demarre alors qu'il monte/descend. **Surete reglementaire CRITIQUE**.

### 5.6 Coherence par train

Reprise de la verification 5.4 par l'analyseur. Detail dans `documentation/livrables/preuves-manuelles.md` tache 3.

### 5.7 Absence de deadlock

A justifier programmatiquement : pour tout marquage atteignable, il existe au moins une transition tirable. L'analyseur verifie en explorant l'espace d'etats accessible. La somme des transitions tirables ci-dessous donne les **40 arcs** du graphe d'accessibilite.

| Marquage | Nombre de transitions tirables | Exemples de transitions tirables |
|----------|-------------------------------:|----------------------------------|
| M0       | 2 | `T1_demande`, `T2_demande` |
| M1       | 2 | `T1_entree_canton`, `T2_demande` |
| M2       | 2 | `T1_demande`, `T2_entree_canton` |
| M3       | 2 | `T1_arrivee_quai`, `T2_demande` |
| M4       | 2 | `T1_entree_canton`, `T2_entree_canton` |
| M5       | 2 | `T1_demande`, `T2_arrivee_quai` |
| M6       | 3 | `T1_depart_quai`, `T2_demande`, `Ouverture_portes_T1` |
| M7       | 1 | `T1_arrivee_quai` |
| M8       | 1 | `T2_arrivee_quai` |
| M9       | 3 | `T1_demande`, `T2_depart_quai`, `Ouverture_portes_T2` |
| M10      | 3 | `T1_depart_quai`, `T2_entree_canton`, `Ouverture_portes_T1` |
| M11      | 2 | `T2_demande`, `Fermeture_portes_T1` |
| M12      | 3 | `T1_entree_canton`, `T2_depart_quai`, `Ouverture_portes_T2` |
| M13      | 2 | `T1_demande`, `Fermeture_portes_T2` |
| M14      | 2 | `T1_depart_quai`, `Ouverture_portes_T1` |
| M15      | 2 | `T2_entree_canton`, `Fermeture_portes_T1` |
| M16      | 2 | `T2_depart_quai`, `Ouverture_portes_T2` |
| M17      | 2 | `T1_entree_canton`, `Fermeture_portes_T2` |
| M18      | 1 | `Fermeture_portes_T1` |
| M19      | 1 | `Fermeture_portes_T2` |

**Resultat** : 0 deadlock sur les 20 marquages (confirme par `Analyseur.deadlocks` qui renvoie une liste vide).

### 5.8 Proprietes LTL

La verification LTL est volontairement bornee au graphe fini (20 noeuds, 40 arcs) : les Safety sont reduites a un predicat vrai sur tous les marquages ; les Liveness `G(p -> F q)` sont reduites a l'existence d'un chemin depuis chaque marquage source `p` vers un marquage cible `q`, sous fairness FIFO des controleurs.

| Propriete LTL | Type | Reduction sur graphe fini | Methode Scala | Resultat |
|---------------|------|---------------------------|---------------|:--------:|
| `G !(T1_sur_canton AND T2_sur_canton)` | Safety | Aucun M0..M19 n'a les deux trains sur le canton | `verifierGSafety` | PASSE |
| `G !(T1_a_quai AND T2_a_quai)` | Safety | Aucun M0..M19 n'a les deux trains a quai | `verifierGSafety` | PASSE |
| `G (Portes_ouvertes -> (T1_a_quai OR T2_a_quai))` | Safety | Les 6 marquages portes ouvertes ont un train a quai | `verifierGSafety(_, verifierSurteOuverturePortes)` | PASSE |
| `G (T1_attente -> F T1_sur_canton)` | Liveness | Depuis tout `T1_attente`, un chemin mene a `T1_sur_canton` | `verifierGFLiveness` | PASSE |
| `G (T2_attente -> F T2_sur_canton)` | Liveness | Depuis tout `T2_attente`, un chemin mene a `T2_sur_canton` | `verifierGFLiveness` | PASSE |

La propriete PSD-Departure est traitee comme invariant critique de tirabilite en 5.5.2 : tout arc `Ti_depart_quai` part d'un marquage avec `Portes_fermees=1`. Elle n'est pas recomptee dans les 5 LTL principales affichees par `Analyseur.main`.

Limite explicite : cette verification n'est pas un model checker LTL generique avec automate de Buchi ; elle est une evaluation directe et defendable sur un systeme fini, borne et entierement enumere.

---

## 6) Comparaison Akka vs Petri

La matrice complete des 3 scenarios (cycle nominal, concurrence canton+quai, surete PSD invalide) est detaillee dans `documentation/livrables/comparaison.md` sections 2 a 4. La sortie reelle de l'analyseur (20 marquages M0..M19, 40 arcs, 5 invariants PASSE, 0 deadlock, 5 LTL PASSE) est synthetisee dans `comparaison.md` section 6.

### 6.1 Synthese

- **Scenario 1 (cycle nominal complet)** : 6 transitions tirees, 6 marquages traverses (M0 -> M1 -> M3 -> M6 -> M11 -> M6 -> M0). Cycle ferme. Tous les marquages traverses appartiennent a l'espace d'etats calcule par l'analyseur.
- **Scenario 2 (concurrence canton+quai)** : 9 transitions tirees, 9 marquages traverses dont M14 et M18 (les marquages "interessants" ou T2 est sur canton pendant que T1 est a quai). Confirmation du tuilage canton/quai sans interference.
- **Scenario 3 (surete PSD invalide)** : depuis M0, l'analyseur enumere les transitions tirables = `{T1_demande, T2_demande}` uniquement. `Ouverture_portes_Ti` n'est pas tirable. Cote Akka, `TrainSpec` verifie que le vrai `Train` n'emet `OuverturePortes` qu'apres autorisation du quai ; `GestionnairePortesSpec` verifie en defense le refus silencieux d'un autre train quand les portes sont deja ouvertes.

### 6.2 Coherence simulation/modele

Chaque transition Petri tirable correspond a un message Akka declencheur identifiable (cf `comparaison.md` section 5, 9 transitions sur 12 explicitement couvertes, les 3 restantes etant strictement symetriques). Reciproquement, chaque message Akka du protocole declenche au plus une transition Petri (les messages `Attente`, `PortesOuvertes`, `PortesFermees` sont des notifications de protocole sans correspondance Petri car internes a la coordination).

**Conclusion** : convergence ligne a ligne entre simulation Akka et modele Petri formel sur les 3 scenarios retenus.

---

## 7) Limites assumees

Les limites suivantes sont des choix delibres du sprint, pas des oublis. Chacune est tracable dans `documentation/gouvernance/protocole-coordination.md` (verrous Q1-Q15) ou dans `documentation/suivi/PLAN.md` section 8.

- **Pas de modelisation de pannes ou de timeouts** : aucun acteur ne crashe, aucun message n'est perdu, aucune supervision strategy au-dela du defaut Akka. Une extension realiste demanderait un modele de defaillance (place `Ti_panne`, transitions de restart) et une analyse de la couverture de fautes.
- **Fairness dependante de l'implementation Akka (FIFO)** : le modele Petri pur autorise tout entrelacement, alors que le code Akka serialise via les files FIFO des controleurs. La propriete de liveness (section 5.8) est argumentee sous cette hypothese de fairness, pas de maniere stricte sur le Petri pur.
- **Pas de duree d'arret a quai** : le delai entre `OuverturePortes` et `FermeturePortes` est un message immediat. Une extension naturelle serait Petri temporise (TPN) ou TLA+.
- **Quai unique** : la M14 reelle a deux quais opposes par station. Notre modele projete sur un seul quai pour garder la combinatoire bornee.
- **Modele PSD simplifie** : un seul niveau de portes. La realite CBTC (IEEE 1474) modelise portes train + portes palieres avec synchronisation electronique.
- **Scope volontairement restreint a 2 trains** : la generalisation a N trains aurait une complexite combinatoire en O(4^N) pour les marquages atteignables, rendant l'enumeration a la main des 5 invariants impraticable pour N > 3.
- **Comparaison Akka vs Petri qualitative** : pas de mesure quantitative (latence, debit). Hors scope du cours.
- **Pas de model checker LTL complet** : nous formalisons les proprietes LTL Safety et Liveness (section 5.8) et evaluons les Safety **directement sur le graphe d'accessibilite fini** dans l'analyseur Scala (`verifierGSafety` et `verifierGFLiveness`, livres en Phase 7 le 30/04). Aucun automate de Buchi, aucun produit synchronise. C'est une limitation explicite, pas un defaut : le sujet du cours autorise cette approche pragmatique pour un systeme borne.
- **Read-arc emule** sur `Portes_fermees` (consume+reproduce) : Petri ordinaire n'a pas de read-arc natif. L'astuce est documentee mais reste une approximation structurelle.

Toutes ces limites sont mentionnees une seconde fois en section 8 sous l'angle "extensions futures" pour distinguer ce qui n'est pas fait de ce qui ne *peut* pas etre fait dans le scope du cours.

---

## 8) Conclusion et extensions futures

### 8.1 Synthese

- **Demontre formellement** : 5 invariants (canton, quai, portes, PSD-Open, PSD-Departure) prouves par induction structurelle et confirmes par enumeration programmatique sur les 20 marquages atteignables (cf `comparaison.md` section 6). Absence de deadlock confirmee. Exclusion mutuelle stricte sur les deux ressources critiques (canton et quai). Surete PSD au sens reglementaire (IEEE 1474, UITP).
- **Demontre experimentalement** : 3 scenarios Akka simules (cycle nominal, concurrence canton+quai, tentative PSD invalide) avec correspondance ligne a ligne au modele Petri sur 9 transitions/12. **49 tests passent** (19 tests Akka : TrainSpec 6 + SectionControllerSpec 3 + QuaiControllerSpec 5 + GestionnairePortesSpec 5 ; 30 tests Petri : AnalyseurSpec).
- **Defendable** : 5 acteurs Akka, 12 places et 12 transitions Petri, 5 invariants, 20 marquages. Tout est enumerable a la main et reproductible programmatiquement. Le rapport de l'analyseur (`comparaison.md` section 6) tient sur une page.
- **Confiance globale** : sous les hypotheses du scope (pas de panne, pas de timeout, 2 trains, 1 canton, 1 quai), le sous-systeme satisfait les proprietes critiques attendues d'un metro automatique avec PSD. Toute defaillance des hypotheses (par exemple panne d'un controleur) sort du scope demontre et necessiterait une nouvelle analyse.

### 8.2 Extensions futures (mentionnees, non implementees)

Classees par cout estime croissant :

1. **Verification LTL programmatique complete** (Phase 7 du PLAN) : `verifierGSafety` et `verifierGFLiveness` ajoutes dans `Analyseur.scala`. Evaluation directe sur le graphe d'accessibilite fini, sans automate de Buchi. Coherent avec section 7. **LIVRE le 30/04/2026** : 3 Safety + 2 Liveness PASSE sur les 20 marquages.
2. **Graphe d'accessibilite avec arcs etiquetes** : `explorerAvecArcs` retourne `(List[Marking], List[Arc])`. Sortie inserable en annexe A1. **LIVRE le 30/04/2026** : 40 arcs etiquetes.
3. **Test d'integration Akka-Petri programmatique** (1-2h) : un nouveau spec qui rejoue la trace de messages d'un scenario Akka comme suite de transitions Petri et compare les marquages. **Non livre** ; la correspondance reste textuelle dans `comparaison.md` sections 2-4.
4. **Petri temporise** (TPN) pour modeliser la duree d'arret a quai. Hors scope du cours (Petri ordinaire suffit pour la verification structurelle).
5. **Generalisation N trains** : refactoring de `Train`, `SectionController`, `QuaiController` en parametriques sur l'ensemble des trains. Complexite combinatoire en O(4^N) pour l'analyseur. Hors sprint.
6. **Second canton en serie ou deux quais opposes** : double les ressources globales et reactive le risque de deadlock croisement. Extension naturelle, hors sprint (verrou `protocole-coordination.md` section 2).
7. **Synchronisation portes train + portes palieres** : modelisation CBTC realiste (IEEE 1474). Doublerait les places et transitions PSD.
8. **Tolerance aux pannes** (supervision Akka, restart strategy, places `Ti_panne` cote Petri). Sujet de cours dedié.
9. **Model checker LTL complet** avec automates de Buchi et produit synchronise. Sujet de cours dedie.
10. **Formalisation TLA+** pour comparaison. Le sujet du cours mentionne TLA+ comme alternative possible. Permettrait de croiser les preuves Petri (structurelle) et TLA+ (LTL native + invariants explicites).

### 8.3 Bilan pedagogique

Le projet illustre concretement :

- Comment la **doctrine "profondeur > complexite"** (PLAN section 8) permet de prouver davantage en simplifiant le modele plutot qu'en l'etendant.
- Comment la **double modelisation** (acteurs Akka pour la simulation, Petri pour la verification) renforce la confiance par convergence.
- Comment une **propriete de surete critique** se traduit a la fois par une machine d'etats cote code (`Train` n'emet `OuverturePortes` qu'a quai), une garde defensive (`GestionnairePortes` refuse un second occupant), et une pre-condition structurelle cote modele (pre `{Ti_a_quai}` de `Ouverture_portes_Ti`). Les deux niveaux se valident mutuellement (triple preuve : structurelle, inductive, programmatique - cf section 5.5).

---

## Annexes

### A1 - Graphe d'accessibilite etiquete

Sortie de l'analyseur Petri : **20 noeuds, 40 arcs etiquetes**.

```text
M0  --T1_demande-->        M1
M0  --T2_demande-->        M2
M1  --T1_entree_canton-->  M3
M1  --T2_demande-->        M4
M2  --T1_demande-->        M4
M2  --T2_entree_canton-->  M5
M3  --T1_arrivee_quai-->   M6
M3  --T2_demande-->        M7
M4  --T1_entree_canton-->  M7
M4  --T2_entree_canton-->  M8
M5  --T1_demande-->        M8
M5  --T2_arrivee_quai-->   M9
M6  --T1_depart_quai-->    M0
M6  --T2_demande-->        M10
M6  --Ouverture_portes_T1--> M11
M7  --T1_arrivee_quai-->   M10
M8  --T2_arrivee_quai-->   M12
M9  --T1_demande-->        M12
M9  --T2_depart_quai-->    M0
M9  --Ouverture_portes_T2--> M13
M10 --T1_depart_quai-->    M2
M10 --T2_entree_canton-->  M14
M10 --Ouverture_portes_T1--> M15
M11 --T2_demande-->        M15
M11 --Fermeture_portes_T1->M6
M12 --T1_entree_canton-->  M16
M12 --T2_depart_quai-->    M1
M12 --Ouverture_portes_T2--> M17
M13 --T1_demande-->        M17
M13 --Fermeture_portes_T2->M9
M14 --T1_depart_quai-->    M5
M14 --Ouverture_portes_T1--> M18
M15 --T2_entree_canton-->  M18
M15 --Fermeture_portes_T1->M10
M16 --T2_depart_quai-->    M3
M16 --Ouverture_portes_T2--> M19
M17 --T1_entree_canton-->  M19
M17 --Fermeture_portes_T2->M12
M18 --Fermeture_portes_T1->M14
M19 --Fermeture_portes_T2->M16
```

### A2 - Matrice Akka / Petri

La matrice detaillee est fournie dans `documentation/livrables/comparaison.md` sections 2 a 5. Elle relie :
- les messages Akka (`Demande`, `Autorisation`, `ArriveeQuai`, `OuverturePortes`, etc.) ;
- les transitions Petri (`Ti_demande`, `Ti_entree_canton`, `Ouverture_portes_Ti`, etc.) ;
- les marquages M0..M19 observes dans les scenarios.

### A3 - Extraits de code a citer

Extraits sources principaux pour la soutenance ou l'export PDF :

| Sujet | Fichier | Role dans la preuve |
|-------|---------|---------------------|
| Machine d'etats du train | `src/main/scala/m14/troncon/Train.scala` | 4 etats (`hors`, `attente`, `sur_canton`, `a_quai`) alignes avec les places Petri |
| Arbitrage canton FIFO | `src/main/scala/m14/troncon/SectionController.scala` | Exclusion mutuelle canton et progression sous FIFO |
| Arbitrage quai FIFO | `src/main/scala/m14/troncon/QuaiController.scala` | Exclusion mutuelle quai et promotion du train en attente |
| Garde PSD | `src/main/scala/m14/troncon/GestionnairePortes.scala` | Refus d'ouverture/fermeture par un train non occupant |
| Reseau Petri | `src/main/scala/m14/petri/PetriNet.scala` | Encodage des 12 places et 12 transitions |
| Analyse BFS + invariants + LTL | `src/main/scala/m14/petri/Analyseur.scala` | Exploration, 40 arcs, invariants, deadlocks, `verifierGSafety`, `verifierGFLiveness` |

### A4 - Noeuds critiques PSD

Marquages avec `Portes_ouvertes=1` : **M11, M13, M15, M17, M18, M19**. Tous contiennent exactement un train a quai :

| Marquage | Train a quai | Sortie obligatoire vers portes fermees |
|----------|--------------|-----------------------------------------|
| M11 | T1 | `Fermeture_portes_T1 -> M6` |
| M13 | T2 | `Fermeture_portes_T2 -> M9` |
| M15 | T1 | `Fermeture_portes_T1 -> M10` |
| M17 | T2 | `Fermeture_portes_T2 -> M12` |
| M18 | T1 | `Fermeture_portes_T1 -> M14` |
| M19 | T2 | `Fermeture_portes_T2 -> M16` |

Ces six noeuds sont les temoins centraux de PSD-Open Safety : aucun marquage n'a `Portes_ouvertes=1` sans train a quai.
