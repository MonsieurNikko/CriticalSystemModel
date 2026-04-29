# Carnet de preuves manuelles - CriticalSystemModel (modele etendu PSD)

> **A qui s'adresse ce document** : aux contributeurs de l'equipe qui ne touchent pas au code Scala mais qui prennent en charge la verification formelle a la main, conformement au cahier des charges (interdiction d'utiliser un outil logiciel de Petri).
>
> **Pourquoi ce travail est central** : le sujet exige une preuve manuelle des proprietes, l'analyseur Scala (livrable L5) ne fait que **confirmer programmatiquement** ce qui aura ete prouve ici. Sans ce carnet rempli, le rapport (L4) n'a pas de fondations.
>
> **Mise a jour majeure 29/04** : carnet etendu pour le modele Canton + Quai + Portes palieres (PSD). 12 places, 12 transitions, ~15-18 marquages atteignables attendus. Le carnet initial a 8 marquages est archive ci-dessous a titre indicatif.

---

## 0) Ce qui est deja fait (a relire d'abord)

Avant de remplir ce carnet, lire dans cet ordre :

1. **`petri/petri-troncon.md`** sections 1 a 8 : definition complete du nouveau reseau (Canton + Quai + PSD), invariants prouves par induction.
2. **`documentation/gouvernance/lexique.md`** : 4 etats par train, 4 ressources globales, 8 messages Akka.
3. **`documentation/gouvernance/protocole-coordination.md`** Q11-Q15 : decisions liees a l'extension PSD.

Tout ce qui suit dans ce carnet **prolonge** ces documents avec les preuves manuelles complementaires.

---

## Tache 1 - Enumeration exhaustive de l'espace d'etats (BFS a la main)

### Objectif

Lister **tous** les marquages atteignables depuis M0 en explorant en largeur, et les indexer M0, M1, M2, ... Le nombre attendu est **15 a 18 marquages** (a confirmer par l'analyseur Scala).

### Methode

A chaque marquage, on liste les transitions tirables (celles dont toutes les places de pre ont un jeton), on calcule les marquages successeurs, on ajoute ceux qui sont nouveaux a la file d'exploration.

**Notation compacte** : on liste seulement les places marquees a 1 (les autres sont a 0). Les ressources globales sont prefixees pour la lisibilite.

### Tableau partiellement pre-rempli (les 9 premiers marquages, le reste est a completer par l'equipe)

| Index | Marquage (places a 1)                                                                  | Atteint via                                | Transitions tirables                                  | Successeurs |
|------:|----------------------------------------------------------------------------------------|--------------------------------------------|-------------------------------------------------------|-------------|
| **M0** | `Canton_libre, Quai_libre, Portes_fermees, T1_hors, T2_hors`                           | (initial)                                  | `T1_demande`, `T2_demande`                            | M1, M2      |
| **M1** | `Canton_libre, Quai_libre, Portes_fermees, T1_attente, T2_hors`                        | M0 + `T1_demande`                          | `T1_entree_canton`, `T2_demande`                      | M3, M4      |
| **M2** | `Canton_libre, Quai_libre, Portes_fermees, T1_hors, T2_attente`                        | M0 + `T2_demande`                          | `T2_entree_canton`, `T1_demande`                      | M5, M4      |
| **M3** | `Quai_libre, Portes_fermees, T1_sur_canton, T2_hors`                                   | M1 + `T1_entree_canton`                    | `T1_arrivee_quai`, `T2_demande`                       | M6, M7      |
| **M4** | `Canton_libre, Quai_libre, Portes_fermees, T1_attente, T2_attente`                     | M1 + `T2_demande` (ou M2 + `T1_demande`)   | `T1_entree_canton`, `T2_entree_canton`                | M7, M8      |
| **M5** | `Quai_libre, Portes_fermees, T1_hors, T2_sur_canton`                                   | M2 + `T2_entree_canton`                    | `T2_arrivee_quai`, `T1_demande`                       | M9, M8      |
| **M6** | `Canton_libre, Portes_fermees, T1_a_quai, T2_hors`                                     | M3 + `T1_arrivee_quai`                     | `Ouverture_portes_T1`, `T2_demande`                   | M10, M11    |
| **M7** | `Quai_libre, Portes_fermees, T1_sur_canton, T2_attente`                                | M3 + `T2_demande` (ou M4 + `T1_entree_canton`) | `T1_arrivee_quai`                                | M11         |
| **M8** | `Quai_libre, Portes_fermees, T1_attente, T2_sur_canton`                                | M4 + `T2_entree_canton` (ou M5 + `T1_demande`) | `T2_arrivee_quai`                                | M12         |
| **M9** | `Canton_libre, Portes_fermees, T1_hors, T2_a_quai`                                     | M5 + `T2_arrivee_quai`                     | `Ouverture_portes_T2`, `T1_demande`                   | M13, M14    |

### Marquages a completer par l'equipe (M10 a M17 estimes)

A explorer en suivant la meme methode :

| Index | Marquage (places a 1)                                                                | Atteint via                                | Transitions tirables                                | Successeurs |
|------:|--------------------------------------------------------------------------------------|--------------------------------------------|-----------------------------------------------------|-------------|
| **M10** | _ (resultat de M6 + Ouverture_portes_T1)                                            | M6 + `Ouverture_portes_T1`                 | `Fermeture_portes_T1`, `T2_demande`                 | _, _        |
| **M11** | _ (resultat de M6 + T2_demande, ou M7 + T1_arrivee_quai)                            | M6 + `T2_demande` ou M7 + `T1_arrivee_quai` | _                                                  | _           |
| **M12** | _                                                                                    | M8 + `T2_arrivee_quai`                     | _                                                   | _           |
| **M13** | _                                                                                    | M9 + `Ouverture_portes_T2`                 | _                                                   | _           |
| **M14** | _                                                                                    | M9 + `T1_demande`                          | _                                                   | _           |
| **M15-M17** | _ (probablement marquages avec Portes_ouvertes + train a quai + autre train sur canton/attente) | _ | _ | _ |

**A verifier par l'equipe** :
- [ ] Tous les marquages avec `Portes_ouvertes=1` doivent **necessairement** contenir `T1_a_quai=1` ou `T2_a_quai=1` (surete PSD).
- [ ] Aucun marquage ne contient `T1_sur_canton=1` ET `T2_sur_canton=1` simultanement (exclusion canton).
- [ ] Aucun marquage ne contient `T1_a_quai=1` ET `T2_a_quai=1` simultanement (exclusion quai).
- [ ] Aucun marquage n'est un deadlock (au moins une transition tirable depuis chaque etat).

**Resultat attendu** : entre **15 et 18 marquages**. Si plus, l'equipe et l'analyseur doivent diverger : appliquer le cas D du protocole-coordination.

---

## Tache 2 - Verification des invariants de ressource

Pour chaque marquage atteignable Mi, verifier les **3 invariants de ressource**.

### 2.1 Invariant canton

Confirmer : `M(T1_sur_canton) + M(T2_sur_canton) + M(Canton_libre) = 1`

| Marquage | T1_sur_canton | T2_sur_canton | Canton_libre | Somme | OK ? |
|----------|--------------:|--------------:|-------------:|------:|:----:|
| M0       | 0 | 0 | 1 | 1 | ✓ |
| M1       | 0 | 0 | 1 | 1 | ✓ |
| M2       | 0 | 0 | 1 | 1 | ✓ |
| M3       | 1 | 0 | 0 | 1 | ✓ |
| M4       | 0 | 0 | 1 | 1 | ✓ |
| M5       | 0 | 1 | 0 | 1 | ✓ |
| M6       | 0 | 0 | 1 | 1 | ✓ |
| M7       | 1 | 0 | 0 | 1 | ✓ |
| M8       | 0 | 1 | 0 | 1 | ✓ |
| M9       | 0 | 0 | 1 | 1 | ✓ |
| M10-M17  | _ | _ | _ | ? | _ a remplir_ |

### 2.2 Invariant quai

Confirmer : `M(T1_a_quai) + M(T2_a_quai) + M(Quai_libre) = 1`

| Marquage | T1_a_quai | T2_a_quai | Quai_libre | Somme | OK ? |
|----------|----------:|----------:|-----------:|------:|:----:|
| M0       | 0 | 0 | 1 | 1 | ✓ |
| M1       | 0 | 0 | 1 | 1 | ✓ |
| M2       | 0 | 0 | 1 | 1 | ✓ |
| M3       | 0 | 0 | 1 | 1 | ✓ |
| M4       | 0 | 0 | 1 | 1 | ✓ |
| M5       | 0 | 0 | 1 | 1 | ✓ |
| M6       | 1 | 0 | 0 | 1 | ✓ |
| M7       | 0 | 0 | 1 | 1 | ✓ |
| M8       | 0 | 0 | 1 | 1 | ✓ |
| M9       | 0 | 1 | 0 | 1 | ✓ |
| M10-M17  | _ | _ | _ | ? | _ a remplir_ |

### 2.3 Invariant portes

Confirmer : `M(Portes_fermees) + M(Portes_ouvertes) = 1`

A remplir par l'equipe pour les 18 marquages. Tous doivent avoir somme = 1.

---

## Tache 2bis (NEW) - Verification des invariants critiques de surete PSD

C'est **le coeur du nouveau livrable**. Sans cette tache, l'extension PSD n'a pas de valeur formelle.

### 2bis.1 Surete PSD-Open : portes ouvertes implique train a quai

Pour chaque marquage Mi tel que `M(Portes_ouvertes) = 1`, verifier que `M(T1_a_quai) + M(T2_a_quai) = 1`.

Liste prevue (a remplir) :

| Marquage avec Portes_ouvertes=1 | T1_a_quai | T2_a_quai | Train a quai ? | OK ? |
|---------------------------------|----------:|----------:|---------------:|:----:|
| M10 (= M6 + Ouverture_T1)       | 1 | 0 | OUI | ✓ |
| M13 (= M9 + Ouverture_T2)       | 0 | 1 | OUI | ✓ |
| _ Autres a identifier_           | _ | _ | _ | _ |

**Conclusion attendue** : tout marquage avec portes ouvertes a strictement un train a quai. Aucune chute possible. **CRITIQUE M14**.

### 2bis.2 Surete PSD-Departure : depart possible implique portes fermees

Pour chaque marquage Mi, identifier si la transition `Ti_depart_quai` est tirable. Si oui, verifier que `M(Portes_fermees) = 1`.

| Marquage | Ti_depart_quai tirable ? | Lequel ? | Portes_fermees ? | OK ? |
|----------|:------------------------:|----------|-----------------:|:----:|
| M6       | OUI | T1_depart_quai (T1_a_quai=1, Portes_fermees=1) | 1 | ✓ |
| M9       | OUI | T2_depart_quai (T2_a_quai=1, Portes_fermees=1) | 1 | ✓ |
| M10      | NON | (Portes_ouvertes=1, donc Portes_fermees=0) | 0 | (transition non tirable, OK) |
| M13      | NON | (Portes_ouvertes=1) | 0 | (transition non tirable, OK) |
| _ Autres_ | _ | _ | _ | _ |

**Conclusion attendue** : un train ne peut quitter le quai que portes fermees. **CRITIQUE M14**.

---

## Tache 3 - Cohérence par train (sur 4 etats au lieu de 3)

Pour chaque train i ∈ {1, 2}, confirmer : `M(Ti_hors) + M(Ti_attente) + M(Ti_sur_canton) + M(Ti_a_quai) = 1`.

### Train 1 (a remplir pour les 18 marquages)

| Marquage | T1_hors | T1_attente | T1_sur_canton | T1_a_quai | Somme |
|----------|--------:|-----------:|--------------:|----------:|------:|
| M0       | 1 | 0 | 0 | 0 | 1 |
| M1       | 0 | 1 | 0 | 0 | 1 |
| M2       | 1 | 0 | 0 | 0 | 1 |
| M3       | 0 | 0 | 1 | 0 | 1 |
| M4       | 0 | 1 | 0 | 0 | 1 |
| M5       | 1 | 0 | 0 | 0 | 1 |
| M6       | 0 | 0 | 0 | 1 | 1 |
| M7       | 0 | 0 | 1 | 0 | 1 |
| M8       | 0 | 1 | 0 | 0 | 1 |
| M9       | 1 | 0 | 0 | 0 | 1 |
| M10-M17  | _ | _ | _ | _ | _ |

### Train 2 (a remplir en miroir)

A remplir par l'equipe.

---

## Tache 4 - Absence de deadlock par exhaustion

Pour chaque marquage Mi, lister au moins une transition tirable. Aucun marquage atteignable ne doit etre un deadlock (sauf eventuellement le marquage initial, mais ici M0 a deja 2 transitions tirables).

| Marquage | Au moins une transition tirable ? | Laquelle ? |
|----------|:---------------------------------:|------------|
| M0       | OUI | `T1_demande` ou `T2_demande` |
| M1       | OUI | `T1_entree_canton` ou `T2_demande` |
| M2       | OUI | `T2_entree_canton` ou `T1_demande` |
| M3       | OUI | `T1_arrivee_quai` ou `T2_demande` |
| M4       | OUI | `T1_entree_canton` ou `T2_entree_canton` |
| M5       | OUI | `T2_arrivee_quai` ou `T1_demande` |
| M6       | OUI | `Ouverture_portes_T1` ou `T2_demande` |
| M7       | OUI | `T1_arrivee_quai` |
| M8       | OUI | `T2_arrivee_quai` |
| M9       | OUI | `Ouverture_portes_T2` ou `T1_demande` |
| M10      | OUI (a confirmer) | `Fermeture_portes_T1` ou `T2_demande` |
| M11-M17  | _ | _ a remplir_ |

**Conclusion attendue** : aucun marquage n'est un deadlock. Le systeme peut toujours progresser.

---

## Tache 5 - Formalisation LTL des proprietes

### Proprietes existantes (a maintenir)

**Safety canton (existant)** :
```
G !(T1_sur_canton AND T2_sur_canton)
```
Lecture : "il n'arrive jamais que les deux trains soient simultanement sur le canton".

**Liveness canton (existant, sous fairness)** :
```
G (T1_attente -> F T1_sur_canton)
G (T2_attente -> F T2_sur_canton)
```

### Proprietes NEW pour le quai et les portes

**Safety quai (NEW)** :
```
G !(T1_a_quai AND T2_a_quai)
```

**Safety PSD-Open (NEW, CRITIQUE)** :
```
G (Portes_ouvertes -> (T1_a_quai OR T2_a_quai))
```
Lecture : "chaque fois que les portes sont ouvertes, un train est a quai". C'est la propriete qui empeche les chutes mortelles.

**Safety PSD-Departure (NEW, CRITIQUE)** :
```
G ((T1_a_quai AND X(T1_hors)) -> Portes_fermees)
G ((T2_a_quai AND X(T2_hors)) -> Portes_fermees)
```
Lecture : "si un train passe de a_quai a hors (donc execute Ti_depart_quai), alors les portes etaient fermees a l'instant du passage". C'est la propriete qui empeche un voyageur d'etre coince.

**Liveness PSD (NEW, sous fairness des portes)** :
```
G (T1_a_quai -> F Portes_ouvertes)
G (T2_a_quai -> F Portes_ouvertes)
```
Lecture : un train a quai finit par avoir les portes ouvertes (les voyageurs peuvent monter/descendre).

### Justification informelle sur l'espace d'etats fini

A remplir par l'equipe pour chaque propriete : l'analyseur Scala enumere les marquages, on verifie la condition. Si elle tient sur tous les marquages, la propriete LTL est satisfaite (justification "model checking sur etat fini").

---

## Tache 6 - Argumentation Liveness sous fairness (etendue)

### Le probleme (rappel)

En Petri pur, depuis un marquage avec 2 trains en attente, l'execution adversaire peut toujours servir le meme train, affamant l'autre.

### La parade (etendue avec PSD)

Cote Akka :
- Le `SectionController` a une file FIFO (existant).
- Le `QuaiController` a une file FIFO (NEW). Quand un train sort du canton et trouve le quai libre, il y entre directement. Sinon il attend dans la file du quai.
- Le `GestionnairePortes` n'a pas de file (un seul train a quai a la fois grace a l'invariant 2.2). Pas de famine sur les portes.

### Limites assumees

A remplir par l'equipe (5-10 lignes) :
- Le modele Petri pur ne garantit pas la Liveness sans hypothese de fairness.
- L'implementation Akka avec FIFO la garantit pour toute sequence finie.
- Sur les 3 scenarios retenus, la Liveness est verifiable par inspection.
- **Limite specifique PSD** : si les portes ne se ferment jamais (bug dans le `GestionnairePortes` ou le `Train`), le train est coince a quai et la fairness echoue. Cas defensif a prouver par tests d'integration.

---

## Tache 7 - Diagramme d'espace d'etats (graphe a la main)

### Objectif

Dessiner un graphe ou les noeuds sont les 15-18 marquages (M0 a M17 estimes) et les arcs sont les transitions. Cette representation visuelle est centrale pour le rapport (livrable L4 section 5).

### Format suggere

Un dessin a la main scanne, ou un diagramme ASCII, ou un schema fait dans un outil de graphe generique (PAS un outil de Petri formel, **interdit** par le cahier des charges).

### Squelette ASCII a etoffer

```
                              M0 (etat initial)
                            /                  \
                       T1_dem                T2_dem
                          /                      \
                        M1                        M2
                       / \                        / \
                T1_ec    T2_dem            T2_ec     T1_dem
                  /        \                /            \
                M3         M4              M5            ...
                 |          |               |
              T1_aq      T1_ec/T2_ec     T2_aq
                 |          |               |
                M6         M7/M8           M9
              / \                          / \
        Ouv_T1  T2_dem                  Ouv_T2  T1_dem
            /     \                       /        \
          M10     M11                   M13         M14
           |                              |
        Ferm_T1                       Ferm_T2
           |                              |
          ...                            ...
```

(Legende : `T1_dem` = `T1_demande`, `T1_ec` = `T1_entree_canton`, `T1_aq` = `T1_arrivee_quai`, `Ouv_T1` = `Ouverture_portes_T1`, `Ferm_T1` = `Fermeture_portes_T1`, idem T2.)

A faire par l'equipe : completer ce graphe avec **toutes** les transitions tirables, y compris les boucles de retour vers M0 via `Ti_depart_quai`.

### Insertion finale

Ce diagramme sera repris dans le rapport (`documentation/livrables/rapport.md` section 5) comme illustration centrale de la verification, avec mise en evidence des marquages "Portes_ouvertes" pour visualiser la propriete de surete.

---

## Synchronisation avec la piste codage

### Quand l'analyseur Scala etendu (livrable L5) sera operationnel (Phase D)

Il devra produire :
- [ ] Une liste de **15-18 marquages** atteignables (correspondance avec la tache 1).
- [ ] Verification des **3 invariants de ressource** sur chaque marquage (taches 2.1, 2.2, 2.3).
- [ ] Verification des **2 invariants critiques PSD** (taches 2bis.1, 2bis.2).
- [ ] Verification des invariants par train sur 4 etats (tache 3).
- [ ] Confirmation qu'aucun marquage n'est un deadlock (tache 4).

Si le code et le carnet divergent, **on debugge le code en priorite** (le carnet est la reference). En cas d'erreur dans le carnet, on corrige et on re-explique dans `documentation/suivi/historique.md`.

### Ou inserer les sorties dans le rapport

| Section du rapport       | Source carnet | Source code |
|--------------------------|---------------|-------------|
| 5.1 Invariant canton     | Tache 2.1     | Sortie analyseur (`verifierInvariantCanton`) |
| 5.2 Invariant quai       | Tache 2.2     | Sortie analyseur (`verifierInvariantQuai`)   |
| 5.3 Invariant portes     | Tache 2.3     | Sortie analyseur (`verifierInvariantPortes`) |
| **5.4 Surete PSD-Open**  | Tache 2bis.1  | Sortie analyseur (`verifierSurteOuverturePortes`) **CRITIQUE** |
| **5.5 Surete PSD-Departure** | Tache 2bis.2 | Sortie analyseur (`verifierSurteDepartQuai`) **CRITIQUE** |
| 5.6 Cohérence par train  | Tache 3       | Sortie analyseur                             |
| 5.7 Absence de deadlock  | Tache 4       | Sortie analyseur                             |
| 5.8 LTL                  | Taches 5 + 6  | (informel, justification sur etat fini)      |
| Annexe A1 (graphe)       | Tache 7       | Sortie brute analyseur                       |

---

## Repartition possible dans l'equipe

Suggestion (modulable selon les disponibilites) :

| Membre        | Taches recommandees |
|---------------|---------------------|
| Contributeur 1 | Taches 1 (enumeration M0-M9) + 5 (LTL) |
| Contributeur 2 | Taches 1 (enumeration M10-M17) + 4 (deadlock) |
| Contributeur 3 | Taches 2 (3 invariants ressources) + 3 (cohérence par train) |
| Contributeur 4 (Nikko) | Code Scala + tache 7 (diagramme final) + integration des resultats dans le rapport |

Les taches 1, 2, 4 sont sequentielles (la 1 produit les marquages que les autres utilisent), les taches 5, 6, 7 sont independantes apres la tache 1. La **tache 2bis (PSD)** est la plus importante academiquement et doit etre relue par 2 contributeurs.

---

## Definition of Done de ce carnet

Le carnet est complet quand :
- [ ] Tous les tableaux sont remplis (pas de `_` ou `?` restants).
- [ ] Le diagramme de la tache 7 est joint (image ou ASCII).
- [ ] Une entree `historique.md` a ete ajoutee a chaque tache terminee.
- [ ] Les resultats convergent avec ceux de l'analyseur Scala en Phase D.
- [ ] **La tache 2bis (PSD) est integralement verifiee et chaque marquage avec `Portes_ouvertes=1` a son train a quai documente.**
- [ ] Les sections 5.1 a 5.8 du rapport ont ete remplies a partir de ce carnet.

---

## Annexe : carnet initial (modele a 7 places, archive)

L'enumeration originale a 8 marquages pour le modele "1 troncon, 7 places, 6 transitions" est conservee a titre historique dans le commit `bf7fbc1` ainsi que dans l'historique git. Elle a servi de base pedagogique avant l'extension PSD.
