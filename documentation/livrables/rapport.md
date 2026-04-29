# Rapport de verification - CriticalSystemModel (modele etendu PSD)

> Livrable L4 du cahier des charges. Rapport detaille de la verification des proprietes structurelles et des invariants metier du sous-systeme critique M14, etendu le 29/04/2026 a la gestion des portes palieres (PSD).
>
> Etat : squelette etendu en Phase A (avant code), sections 1-4 et 7-8 a finaliser apres execution Phase D, section 5 (verification) a remplir avec les sorties d'analyseur en Phase D. Longueur cible 12-18 pages a l'export PDF.

---

## 1) Contexte et motivation

> A finaliser en Phase D apres execution complete.

Points a couvrir :
- Rappel du domaine applicatif : metro automatique parisien M14, premiere ligne entierement automatisee de France, premiere a etre integralement equipee de **portes palieres (PSD - Platform Screen Doors)**. Sous-systeme retenu : controle d'acces concurrent de 2 trains a un canton de signalisation suivi d'un quai equipe de PSD.
- Pourquoi un systeme distribue critique : une defaillance entraine soit collision (2 trains sur le canton), soit chute mortelle (portes ouvertes sans train), soit voyageur ecrase (depart avec portes ouvertes).
- Resume du recadrage du projet (cf `documentation/contexte/recadrage-m14-troncon-critique.md`).
- Choix d'abstraction : 2 trains, 1 canton, 1 quai, 1 paire de PSD, 4 acteurs (2 trains + 3 controleurs). Justification du minimalisme defendable et de l'extension PSD validee le 29/04 comme moyen de rendre le projet plus M14-realiste sans exploser la combinatoire.

---

## 2) Bibliographie

> A finaliser en Phase D a partir de `documentation/livrables/biblio.md` (10-11 sources apres ajouts PSD).

Sources regroupees par theme : reseaux de Petri (Murata, David & Alla), modele acteur (Akka doc, Hewitt), verification formelle (Baier & Katoen, Manna & Pnueli, LTL TUM), concurrence (Magee & Kramer, Lamport), domaine ferroviaire critique (RATP M14, IEEE std 1474 CBTC, etudes PSD). Chaque source est citee au moins une fois dans le corps du rapport.

---

## 3) Architecture Akka

> A rediger en Phase D apres ecriture du code.

Points a couvrir :
- **Diagramme des acteurs** : 2 `Train` + 1 `SectionController` (canton) + 1 `QuaiController` (quai) + 1 `GestionnairePortes` (PSD). 5 acteurs au total (vs 3 dans le modele initial).
- Description des `Behavior[T]` typés et de leur cycle de vie. La machine d'etats du `Train` a **4 etats** : `comportementHors`, `comportementEnAttente`, `comportementSurCanton`, `comportementAQuai` (cf `documentation/gouvernance/lexique.md` section 1).
- **Protocole de messages** : 12 messages au total (cf `petri/petri-troncon.md` section 8 et `documentation/gouvernance/lexique.md` section 3).
  - Vers controleurs : `Demande`, `Sortie`, `ArriveeQuai`, `DepartQuai`, `OuverturePortes`, `FermeturePortes`.
  - Vers trains : `Autorisation`, `Attente`, `PortesOuvertes`, `PortesFermees`.
- Logique d'arbitrage du `SectionController` : etat `cantonLibre` / `cantonOccupe(occupant, file)` + file FIFO immuable.
- Logique d'arbitrage du `QuaiController` : symetrique, `quaiLibre` / `quaiOccupe(occupant, file)`. Duplication de design assumee (cf `documentation/gouvernance/protocole-coordination.md` Q11).
- **Garde de surete CRITIQUE du `GestionnairePortes`** : refuse toute demande d'ouverture si aucun train n'est a quai. Cette garde est verifiee par test unitaire dedie (`GestionnairePortesSpec`).
- Justification des hypotheses : pas de panne, pas de timeout, pas de duree d'arret a quai (toutes en "extensions futures" section 8).

---

## 4) Modele Petri formel

> A rediger en Phase D a partir de `petri/petri-troncon.md`.

Points a couvrir :
- Les **12 places** et leur interpretation metier (4 ressources globales + 4 etats T1 + 4 etats T2).
- Les **12 transitions effectives**, leur condition de tirabilite, leur effet sur le marquage. Detail des transitions PSD : `Ouverture_portes_Ti`, `Fermeture_portes_Ti`.
- **Marquage initial M0** (5 jetons : `Canton_libre, Quai_libre, Portes_fermees, T1_hors, T2_hors`).
- **Read-arc emule** sur la place `Portes_fermees` dans les transitions `Ti_depart_quai` (consommation+reproduction). Justification : Petri ordinaire n'a pas de read-arc natif.
- Pourquoi 12 places et 12 transitions, pas plus (lien avec le scope verrouille `documentation/gouvernance/protocole-coordination.md` section 2).
- Schema ASCII repris depuis `petri/petri-troncon.md` section 4.

---

## 5) Verification des proprietes (coeur du rapport)

### 5.1 Invariant canton

Enonce : `M(T1_sur_canton) + M(T2_sur_canton) + M(Canton_libre) = 1` pour tout marquage atteignable.

Preuve a deux niveaux :
- **A la main** : verification sur M0 et preservation par chaque transition (cf `petri/petri-troncon.md` section 5.1 et `documentation/livrables/preuves-manuelles.md` tache 2.1).
- **Programmatique** : confirmation par l'analyseur Scala (`verifierInvariantCanton`) sur tous les ~15-18 marquages atteignables. Sortie a inserer en Phase D.

**Consequence** : exclusion mutuelle stricte sur le canton de signalisation. Pas de collision possible.

### 5.2 Invariant quai (NEW)

Enonce : `M(T1_a_quai) + M(T2_a_quai) + M(Quai_libre) = 1` pour tout marquage atteignable.

Meme structure de preuve. Fonction analyseur : `verifierInvariantQuai`.

**Consequence** : exclusion mutuelle stricte sur le quai. Pas de collision en station.

### 5.3 Invariant portes (NEW)

Enonce : `M(Portes_fermees) + M(Portes_ouvertes) = 1` pour tout marquage atteignable.

Meme structure de preuve. Fonction analyseur : `verifierInvariantPortes`.

**Consequence** : les portes palieres sont toujours dans exactement un etat (ouvertes ou fermees), jamais dans un etat indetermine. Propriete de coherence d'etat.

### 5.4 Invariants par train (sur 4 etats)

Enonce : `M(Ti_hors) + M(Ti_attente) + M(Ti_sur_canton) + M(Ti_a_quai) = 1` pour i in {1,2}, sur tout marquage atteignable.

Meme structure de preuve. Generalisation des 3 etats du modele initial.

### 5.5 Surete PSD (CRITIQUE - coeur de l'extension)

#### 5.5.1 PSD-Open Safety (portes ouvertes implique train a quai)

Enonce LTL : `G ( Portes_ouvertes = 1 => T1_a_quai + T2_a_quai = 1 )`.

**Triple preuve** (cf `documentation/gouvernance/protocole-coordination.md` Q15) :

1. **Structurelle** : la place `Portes_ouvertes` ne peut etre marquee que par les transitions `Ouverture_portes_Ti`, dont le pre contient `Ti_a_quai`. Donc tant que `Portes_ouvertes=1`, un train est a quai. La preuve est inductive sur les transitions.

2. **Inductive** : par recurrence sur le marquage initial (M0 a `Portes_ouvertes=0`, donc l'implication est trivialement vraie) et chaque transition. Detail dans `documentation/livrables/preuves-manuelles.md` tache 2bis.1.

3. **Programmatique** : l'analyseur Scala (`verifierSurteOuverturePortes`) enumere les marquages atteignables et verifie pour chacun l'implication. Si la verification echoue sur un seul marquage, l'analyseur signale une violation.

**Consequence M14** : les voyageurs ne peuvent jamais tomber sur les voies via les portes palieres. **Surete reglementaire CRITIQUE**.

#### 5.5.2 PSD-Departure Safety (depart implique portes fermees)

Enonce : pour tout marquage M et tout train Ti, si `Ti_depart_quai` est tirable depuis M, alors `M(Portes_fermees)=1`.

**Justification structurelle** : c'est inscrit dans le pre de `Ti_depart_quai` : `{Ti_a_quai, Portes_fermees}`. La transition n'est pas tirable sinon. Verification programmatique : `verifierSurteDepartQuai` enumere les marquages et verifie la condition.

**Consequence M14** : aucun voyageur ne peut etre coince ou ecrase par un train qui demarre alors qu'il monte/descend. **Surete reglementaire CRITIQUE**.

### 5.6 Coherence par train

Reprise de la verification 5.4 par l'analyseur. Detail dans `documentation/livrables/preuves-manuelles.md` tache 3.

### 5.7 Absence de deadlock

A justifier programmatiquement : pour tout marquage atteignable, il existe au moins une transition tirable. L'analyseur verifie en explorant l'espace d'etats accessible. Detail dans `documentation/livrables/preuves-manuelles.md` tache 4.

**Resultat attendu** : 0 deadlock sur les ~15-18 marquages.

### 5.8 Proprietes LTL

Formalisation complete dans `documentation/livrables/preuves-manuelles.md` tache 5. Synthese :

**Safety** :
- Canton : `G !(T1_sur_canton AND T2_sur_canton)`
- Quai : `G !(T1_a_quai AND T2_a_quai)`
- **PSD-Open** : `G ( Portes_ouvertes -> (T1_a_quai OR T2_a_quai) )`
- **PSD-Departure** : `G ( (Ti_a_quai AND X(Ti_hors)) -> Portes_fermees )`

**Liveness** (sous fairness des controleurs FIFO) :
- Canton : `G ( Ti_attente -> F Ti_sur_canton )`
- PSD : `G ( Ti_a_quai -> F Portes_ouvertes )` (les voyageurs peuvent monter/descendre)

Justification informelle sur l'espace d'etats fini : l'analyseur enumere les marquages, on verifie la condition. Si elle tient sur tous, la propriete LTL est satisfaite (model checking sur etat fini).

---

## 6) Comparaison Akka vs Petri

> A finaliser en Phase D a partir de `documentation/livrables/comparaison.md`.

Reprendre la matrice 3 scenarios x (message Akka, transition Petri, marquage) :
- Scenario 1 : cycle nominal complet (1 train, canton + quai + ouverture/fermeture portes + depart).
- Scenario 2 : concurrence canton+quai (Train1 a quai, Train2 sur canton, portes ouvertes pour T1).
- Scenario 3 : tentative d'ouverture invalide (rejet par garde de surete + non-tirabilite Petri).

Conclure sur la coherence entre simulation et modele formel : chaque transition Petri tirable a un message Akka declencheur et reciproquement.

---

## 7) Limites assumees

> A rediger en Phase D.

- **Pas de modelisation de pannes ou de timeouts** : aucun acteur ne crashe, aucun message n'est perdu.
- **Fairness dependante de l'implementation Akka (FIFO)**, pas du modele Petri pur.
- **Pas de duree d'arret a quai** modelisee : le delai d'embarquement est un message immediat.
- **Quai unique** : modelisation simplifiee (en realite M14 a 2 quais opposes par station).
- **Modele PSD simplifie** : un seul niveau de portes (en realite portes train + portes palieres synchronisees electroniquement).
- Comparaison qualitative et non quantitative.
- Scope volontairement restreint a 2 trains.
- Pas de model checker LTL complet (verification structurelle + LTL informelle sur espace d'etats fini).

---

## 8) Conclusion et extensions futures

> A rediger en Phase D.

Synthese :
- **Demonstre formellement** : 5 invariants (canton, quai, portes, PSD-Open, PSD-Departure) prouves par induction et confirmes par enumeration programmatique. Absence de deadlock. Exclusion mutuelle stricte sur 2 ressources critiques. Surete PSD au sens reglementaire.
- **Demonstre experimentalement** : 3 scenarios Akka simules avec correspondance ligne a ligne au modele Petri.
- **Confiance globale** : sous les hypotheses du scope (pas de panne, pas de timeout, 2 trains, 1 canton, 1 quai), le sous-systeme satisfait les proprietes critiques attendues d'un metro automatique avec PSD.

Extensions possibles (mentionnees, non implementees) :
- N trains (generalisation, complexite combinatoire en O(4^N)).
- 2 quais opposes (extension naturelle, double les ressources globales).
- Synchronisation portes train + portes palieres (CBTC realiste).
- Duree d'arret a quai (Petri temporise ou TLA+).
- Tolerance aux pannes (supervision Akka, restart strategy).
- Model checker LTL complet avec automates de Buchi.
- Formalisation TLA+ pour comparaison (le sujet du cours mentionne TLA+ comme alternative possible).

---

## Annexes

- **A1** : sortie complete de l'analyseur Petri Phase D (15-18 marquages, arcs etiquetes, 5 invariants verifies, 0 deadlock).
- **A2** : matrice de comparaison detaillee Akka/Petri (extrait de `documentation/livrables/comparaison.md`).
- **A3** : extraits de code commentes (un Behavior par etat du Train sur 4 etats, garde de surete du `GestionnairePortes`, BFS de l'analyseur, fonctions `verifierSurteOuverturePortes` et `verifierSurteDepartQuai`).
- **A4** : graphe d'accessibilite de la tache 7 du carnet de preuves manuelles (15-18 noeuds, arcs etiquetes par transition, mise en evidence des marquages avec `Portes_ouvertes`).
