# Rapport de verification - CriticalSystemModel

> Livrable L4 du cahier des charges. Rapport detaille de la verification des proprietes structurelles et des invariants metier du sous-systeme critique M14.
>
> Etat : squelette cree en Phase 2 (anticipation). Sections a remplir au fil des phases 7 et 8. Longueur cible 8-15 pages a l'export PDF.

---

## 1) Contexte et motivation

> A rediger en Phase 8.

Points a couvrir :
- Rappel du domaine applicatif (metro automatique M14, sous-systeme troncon partage).
- Pourquoi un systeme critique distribue, et pourquoi la verification formelle est pertinente.
- Resume du recadrage du projet (cf. `documentation/contexte/recadrage-m14-troncon-critique.md`).
- Choix d'abstraction : 2 trains, 1 troncon, 1 controleur. Justification du minimalisme defendable.

---

## 2) Bibliographie

> A finaliser en Phase 8 a partir de `documentation/livrables/biblio.md`.

Resumer les sources utilisees, regroupees par theme : Petri, Akka, LTL, concurrence, transport critique. Citer chaque source au moins une fois dans le corps du rapport.

---

## 3) Architecture Akka

> A rediger en Phase 8.

Points a couvrir :
- Diagramme des acteurs : 2 `Train` + 1 `SectionController`.
- Description des `Behavior[T]` typés et de leur cycle de vie.
- Protocole de messages : `Demande`, `Sortie`, `Autorisation`, `Attente`. Source : `petri/petri-troncon.md` section 8 et `documentation/gouvernance/lexique.md` section 3.
- Logique d'arbitrage du controleur : etat libre / occupe + file FIFO.
- Justification des hypotheses (un seul controleur centralise, pas de panne, pas de timeout).

---

## 4) Modele Petri formel

> A rediger en Phase 8 a partir de `petri/petri-troncon.md`.

Points a couvrir :
- Les 7 places et leur interpretation metier.
- Les 6 transitions, leur condition de tirabilite, leur effet sur le marquage.
- Marquage initial et son interpretation.
- Pourquoi 7 places et 6 transitions, pas plus, pas moins (lien avec le scope recadre).
- Schema ASCII repris depuis `petri/petri-troncon.md`.

---

## 5) Verification des proprietes

### 5.1 Invariant principal

Enoncé : `M(T1_sur_troncon) + M(T2_sur_troncon) + M(Troncon_libre) = 1` pour tout marquage atteignable.

Preuve a deux niveaux :
- **A la main** : verification sur M0 et preservation par chaque transition (cf. `petri/petri-troncon.md` section 5).
- **Programmatique** : confirmation par l'analyseur Scala sur tous les marquages atteignables (livrable L5, sortie a inserer ici en Phase 6/7).

### 5.2 Invariants par train

Enoncé : `M(Ti_hors) + M(Ti_attente) + M(Ti_sur_troncon) = 1` pour i ∈ {1, 2}, sur tout marquage atteignable.

Meme structure de preuve.

### 5.3 Absence de deadlock

A justifier programmatiquement : pour tout marquage atteignable, il existe au moins une transition tirable. L'analyseur le verifie en explorant l'espace d'etats accessible.

### 5.4 Proprietes LTL

> A rediger en Phase 7.

Safety : `G !(T1_sur_troncon ∧ T2_sur_troncon)`. Justifier sur l'espace d'etats fini (l'analyseur verifie qu'aucun marquage atteignable ne contient simultanement les deux jetons).

Liveness : `G (T1_attente -> F T1_sur_troncon)` sous hypothese de fairness (FIFO controleur Akka). Discuter le decalage entre le modele Petri non deterministe et la garantie pratique cote Akka.

---

## 6) Comparaison Akka vs Petri

> A rediger en Phase 8 a partir de `documentation/livrables/comparaison.md`.

Reprendre la matrice scenarios x (message, transition, marquage) et conclure sur la coherence entre simulation et modele formel.

---

## 7) Limites assumees

> A rediger en Phase 8.

- Pas de modelisation de pannes ou de timeouts.
- Fairness dependante de l'implementation Akka, pas du modele Petri pur.
- Comparaison qualitative et non quantitative.
- Scope volontairement restreint a 2 trains.
- Pas de model checker complet : verification structurelle + LTL informelle sur espace d'etats fini.

---

## 8) Conclusion et extensions futures

> A rediger en Phase 8.

Synthese :
- Ce qui est demontre formellement.
- Ce qui est demontre experimentalement (simulation Akka).
- Confiance globale dans le sous-systeme.

Extensions possibles (mentionnees, non implementees) :
- N trains (generalisation).
- Tolerance aux pannes (supervision Akka).
- Model checker LTL complet.
- Modele temporise (Petri temporel ou TLA+).

---

## Annexes

- A1 : sortie complete de l'analyseur Petri (a coller en Phase 6).
- A2 : matrice de comparaison detaillee (extrait de `documentation/livrables/comparaison.md`).
- A3 : extraits de code commentes (un Behavior par etat, le BFS de l'analyseur).
