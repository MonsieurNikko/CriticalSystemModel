# Carnet de preuves manuelles - CriticalSystemModel

> **A qui s'adresse ce document** : aux contributeurs de l'equipe qui ne touchent pas au code Scala mais qui prennent en charge la verification formelle a la main, conformement au cahier des charges (interdiction d'utiliser un outil logiciel de Petri).
>
> **Pourquoi ce travail est central** : le sujet exige une preuve manuelle des proprietes, l'analyseur Scala (livrable L5) ne fait que **confirmer programmatiquement** ce qui aura ete prouve ici. Sans ce carnet rempli, le rapport (L4) n'a pas de fondations.
>
> **Quand commencer** : maintenant. Ce travail est independant du code. Il converge avec le code en Phase 6 quand on compare les sorties de l'analyseur a ce carnet.

---

## 0) Ce qui est deja fait (a relire d'abord)

Avant de remplir ce carnet, lire dans cet ordre :

1. **`petri/petri-troncon.md`** sections 1 a 5 : definition complete du reseau, invariant principal prouve sur chaque transition.
2. **`documentation/lexique.md`** : pour ne jamais s'eloigner du vocabulaire commun.
3. **`documentation/comparaison.md`** sections 2 a 4 : matrice scenarios x (message Akka, transition Petri, marquage).

Tout ce qui suit dans ce carnet **prolonge** ces documents avec les preuves manuelles complementaires.

---

## Tache 1 - Enumeration exhaustive de l'espace d'etats (par BFS a la main)

### Objectif

Lister **tous** les marquages atteignables depuis M0 en explorant en largeur, et les indexer M0, M1, M2, ... pour pouvoir y referer.

### Methode

A chaque marquage, on liste les transitions tirables (celles dont toutes les places d'entree ont un jeton), on calcule les marquages successeurs, on ajoute ceux qui sont nouveaux a la file d'exploration.

Notation compacte : on liste seulement les places marquees a 1 (les autres sont a 0).

### Tableau a remplir

| Index | Marquage (places marquees a 1) | Atteint via      | Transitions tirables depuis ce marquage | Successeurs |
|------:|--------------------------------|------------------|-----------------------------------------|-------------|
| **M0** | `Troncon_libre, T1_hors, T2_hors`         | (initial)            | `T1_demande`, `T2_demande` | M1, M2 |
| **M1** | `Troncon_libre, T1_attente, T2_hors`      | M0 + `T1_demande`    | `T1_entree_autorisee`, `T2_demande` | M3, M4 |
| **M2** | `Troncon_libre, T1_hors, T2_attente`      | M0 + `T2_demande`    | `T2_entree_autorisee`, `T1_demande` | M5, M4 |
| **M3** | `T1_sur_troncon, T2_hors`                  | M1 + `T1_entree_autorisee` | `T1_sortie_liberation`, `T2_demande` | M0 (via sortie), M6 |
| **M4** | `Troncon_libre, T1_attente, T2_attente`   | M1 + `T2_demande` (ou M2 + `T1_demande`) | `T1_entree_autorisee`, `T2_entree_autorisee` | M6, M7 |
| **M5** | `T2_sur_troncon, T1_hors`                  | M2 + `T2_entree_autorisee` | `T2_sortie_liberation`, `T1_demande` | M0 (via sortie), M7 |
| **M6** | `T1_sur_troncon, T2_attente`               | M3 + `T2_demande` (ou M4 + `T1_entree_autorisee`) | `T1_sortie_liberation` | M2 (via sortie) |
| **M7** | `T2_sur_troncon, T1_attente`               | M5 + `T1_demande` (ou M4 + `T2_entree_autorisee`) | `T2_sortie_liberation` | M1 (via sortie) |

**A verifier par l'equipe** :
- [ ] Chaque ligne est correcte : la transition citee est bien tirable depuis le marquage precedent.
- [ ] Chaque successeur est bien deja indexe (pas de marquage manquant).
- [ ] Aucun nouveau marquage n'apparait au-dela de M7 (l'exploration est close).

**Resultat attendu** : exactement **8 marquages atteignables**. C'est la valeur de reference que l'analyseur Scala devra reproduire (Phase 6).

---

## Tache 2 - Verification de l'invariant principal sur chaque marquage

### Objectif

Confirmer que `M(T1_sur_troncon) + M(T2_sur_troncon) + M(Troncon_libre) = 1` sur **chacun** des 8 marquages enumeres.

### Tableau a remplir

| Marquage | `T1_sur_troncon` | `T2_sur_troncon` | `Troncon_libre` | Somme | OK ? |
|----------|------------------:|------------------:|-----------------:|------:|:----:|
| M0       | 0 | 0 | 1 | 1 | ✓ |
| M1       | 0 | 0 | 1 | 1 | ✓ |
| M2       | 0 | 0 | 1 | 1 | ✓ |
| M3       | 1 | 0 | 0 | 1 | ✓ |
| M4       | 0 | 0 | 1 | 1 | ✓ |
| M5       | 0 | 1 | 0 | 1 | ✓ |
| M6       | 1 | 0 | 0 | 1 | ✓ |
| M7       | 0 | 1 | 0 | 1 | ✓ |

**Conclusion attendue** : l'invariant tient sur les 8 marquages. Aucun marquage ne contient simultanement `T1_sur_troncon` ET `T2_sur_troncon` a 1. Donc **l'exclusion mutuelle est garantie**.

### Verification croisee de la preuve par induction (deja faite)

La preuve par induction (chaque transition preserve la somme) est dans `petri/petri-troncon.md` section 5. La verification ci-dessus est la **preuve par enumeration**. Les deux concordent : c'est la convergence des deux methodes qui donne la confiance.

---

## Tache 3 - Verification des invariants par train

### Objectif

Confirmer que pour chaque train i ∈ {1, 2}, sur chaque marquage : `M(Ti_hors) + M(Ti_attente) + M(Ti_sur_troncon) = 1`.

### Tableau a remplir pour Train 1

| Marquage | `T1_hors` | `T1_attente` | `T1_sur_troncon` | Somme |
|----------|----------:|-------------:|-----------------:|------:|
| M0       | 1 | 0 | 0 | 1 |
| M1       | 0 | 1 | 0 | 1 |
| M2       | 1 | 0 | 0 | 1 |
| M3       | 0 | 0 | 1 | 1 |
| M4       | 0 | 1 | 0 | 1 |
| M5       | 1 | 0 | 0 | 1 |
| M6       | 0 | 0 | 1 | 1 |
| M7       | 0 | 1 | 0 | 1 |

### Tableau a remplir pour Train 2 (a faire par l'equipe en miroir)

| Marquage | `T2_hors` | `T2_attente` | `T2_sur_troncon` | Somme |
|----------|----------:|-------------:|-----------------:|------:|
| M0       | _ | _ | _ | ? |
| M1       | _ | _ | _ | ? |
| M2       | _ | _ | _ | ? |
| M3       | _ | _ | _ | ? |
| M4       | _ | _ | _ | ? |
| M5       | _ | _ | _ | ? |
| M6       | _ | _ | _ | ? |
| M7       | _ | _ | _ | ? |

**A faire** : un membre de l'equipe remplit ce tableau et confirme que toutes les sommes valent 1.

---

## Tache 4 - Preuve d'absence de deadlock par exhaustion

### Objectif

Demontrer qu'aucun marquage atteignable n'est un deadlock. Un deadlock serait un marquage **different de M0** dans lequel **aucune** transition n'est tirable.

### Tableau a remplir

Pour chaque marquage Mi, lister au moins une transition tirable :

| Marquage | Au moins une transition tirable ? | Laquelle ? |
|----------|:---------------------------------:|------------|
| M0       | Oui | `T1_demande` (ou `T2_demande`) |
| M1       | Oui | `T1_entree_autorisee` (ou `T2_demande`) |
| M2       | Oui | `T2_entree_autorisee` (ou `T1_demande`) |
| M3       | Oui | `T1_sortie_liberation` (ou `T2_demande`) |
| M4       | Oui | `T1_entree_autorisee` (ou `T2_entree_autorisee`) |
| M5       | Oui | `T2_sortie_liberation` (ou `T1_demande`) |
| M6       | Oui | `T1_sortie_liberation` |
| M7       | Oui | `T2_sortie_liberation` |

**Conclusion attendue** : aucun marquage atteignable n'est un deadlock. Le systeme peut toujours progresser.

**Note importante** : ceci ne prouve pas la **liveness individuelle** (un train peut-il toujours sortir de l'attente ?). C'est l'objet de la tache 6.

---

## Tache 5 - Formalisation LTL des proprietes

### Objectif

Exprimer en logique LTL les proprietes que le systeme doit verifier. Travail de papier, pas de code.

### Rappel des operateurs LTL

- `G p` : "globalement p" - p est vrai a tout moment.
- `F p` : "finalement p" - p sera vrai a un moment.
- `X p` : "au prochain pas p".
- `p U q` : "p jusqu'a q" - p est vrai jusqu'a ce que q devienne vrai.

### Proprietes a formaliser

**Safety 1 - Exclusion mutuelle** :
```
G ¬ (T1_sur_troncon ∧ T2_sur_troncon)
```
Lecture : "il n'arrive jamais que les deux trains soient simultanement sur le troncon".

**Safety 2 - Invariant principal (forme LTL)** :
```
G (T1_sur_troncon + T2_sur_troncon + Troncon_libre = 1)
```
Lecture : "a tout instant, exactement une des trois places est marquee".

**Liveness 1 - Progression de Train 1** :
```
G (T1_attente → F T1_sur_troncon)
```
Lecture : "chaque fois que Train 1 est en attente, il finira par etre sur le troncon".
**Hypothese** : fairness sur l'arbitrage du controleur (il finit toujours par servir Train 1).

**Liveness 2 - Progression de Train 2 (a remplir par l'equipe)** :
```
G (... → F ...)
```

### Justification informelle sur l'espace d'etats fini

Pour chaque propriete, expliquer en 3-5 phrases pourquoi elle tient ou non sur l'espace d'etats des taches 1-2.

A remplir :
- Safety 1 : verifie par enumeration (tache 2). Aucun marquage ne contient les deux jetons. _OK._
- Safety 2 : equivalent a l'invariant principal. _OK._
- Liveness 1 : ... _(a remplir)_
- Liveness 2 : ... _(a remplir)_

---

## Tache 6 - Argumentation Liveness sous fairness

### Objectif

Discuter honnetement la propriete de liveness. C'est la partie la plus subtile et celle ou un correcteur exigeant cherchera des trous.

### Le probleme

Dans le modele Petri pur, depuis M4 = `(Troncon_libre, T1_attente, T2_attente)`, **les deux** transitions `T1_entree_autorisee` et `T2_entree_autorisee` sont tirables. Le modele Petri ne dit pas laquelle est choisie. Si une execution adversaire choisit toujours `T1_...` quand T2 attend, T2 attend pour toujours. C'est de la **famine** (absence de Liveness).

### La parade

Cote Akka, le `SectionController` maintient une **file FIFO**. Quand T1 et T2 demandent dans cet ordre, T1 est servi en premier. Quand T1 sort et que T2 est dans la file, T2 est obligatoirement servi avant qu'une nouvelle demande de T1 puisse passer.

### Ce qu'on peut prouver, ce qu'on ne peut pas

A remplir par l'equipe en redaction libre (5-10 lignes) :

**Ce qu'on peut affirmer** :
- Le modele Petri pur ne garantit pas la Liveness sans hypothese supplementaire.
- L'implementation Akka avec file FIFO garantit la Liveness pour toute sequence finie de demandes.
- Sur les 3 scenarios retenus, la Liveness est verifiable directement par inspection.

**Ce qu'on ne peut pas affirmer (a assumer comme limite)** :
- ...
- ...
- ...

### Resultat attendu

Cette discussion doit alimenter directement la section "Limites assumees" du rapport (`documentation/rapport.md` section 7).

---

## Tache 7 - Diagramme de l'espace d'etats (graphique a la main)

### Objectif

Dessiner un graphe ou les noeuds sont les 8 marquages (M0 a M7) et les arcs sont les transitions. C'est la representation visuelle de tout le travail des taches 1-4.

### Format suggere

Un dessin a la main scanne, ou un diagramme ASCII, ou un schema fait dans n'importe quel outil (sauf un outil de Petri formel - **interdit** par le cahier des charges).

Squelette ASCII a etoffer :

```
                    M0
                  /    \
            T1_dem     T2_dem
              /            \
            M1              M2
           / \              / \
       T1_ea  T2_dem    T2_ea  T1_dem
         /      \        /       \
        M3       M4 ====        M5
         |      / \              |
       T1_sl  T1_ea T2_ea      T2_sl
         |    /     \            |
        ...  M6     M7          ...
```

(Legende : `T1_dem` = `T1_demande`, `T1_ea` = `T1_entree_autorisee`, `T1_sl` = `T1_sortie_liberation`, idem pour T2.)

A faire : completer ce graphe en y faisant figurer **toutes** les transitions tirables depuis chaque marquage, y compris les boucles de retour vers M0 via les transitions de sortie.

### Insertion finale

Ce diagramme sera repris dans le rapport (`documentation/rapport.md` section 5) comme illustration centrale de la verification.

---

## Synchronisation avec la piste codage

### Quand l'analyseur Scala (livrable L5) sera operationnel (Phase 6)

Il devra produire :
- [ ] Une liste de 8 marquages atteignables (correspondance avec la tache 1).
- [ ] Pour chaque marquage, la verification de l'invariant principal (correspondance avec la tache 2).
- [ ] Pour chaque marquage, la verification des invariants par train (correspondance avec la tache 3).
- [ ] La confirmation qu'aucun marquage n'est un deadlock (correspondance avec la tache 4).

Si le code et le carnet divergent, **on debugge le code en priorite** (le carnet est la reference). En cas d'erreur dans le carnet, on corrige et on re-explique dans `documentation/historique.md`.

### Ou inserer les sorties dans le rapport

| Section du rapport | Source carnet | Source code |
|--------------------|---------------|-------------|
| 5.1 Invariant principal | Tache 2 | Sortie analyseur (Phase 6) |
| 5.2 Invariants par train | Tache 3 | Sortie analyseur |
| 5.3 Absence de deadlock | Tache 4 | Sortie analyseur |
| 5.4 Proprietes LTL | Taches 5 et 6 | (informel, pas de model checker code) |
| Annexe A1 | Tache 7 (diagramme) | Sortie brute analyseur |

---

## Repartition possible dans l'equipe

| Membre        | Taches recommandees |
|---------------|---------------------|
| Axelobistro   | Taches 1 (enumeration) + 5 (LTL) |
| Alicette     | Taches 2 + 3 (verification invariants) + 6 (Liveness sous fairness) |
| Nikko (toi)  | Code Scala (SectionController, Train, analyseur) - **n'a pas a faire ces taches a la main**, mais relit en Phase 6 |
| Ostreann     | Taches 4 (deadlock) + 7 (diagramme final) + integration des resultats dans le rapport |

Un membre peut commencer immediatement, sans attendre que les autres aient termine. Les taches 1-2-3-4 sont sequentielles (la 1 produit les marquages que les autres utilisent), les taches 5-6-7 sont independantes apres la tache 1.

---

## Definition of Done de ce carnet

Le carnet est complet quand :
- [ ] Tous les tableaux sont remplis (pas de `_` ou `?` restants).
- [ ] Le diagramme de la tache 7 est joint (image ou ASCII).
- [ ] Une entree `historique.md` a ete ajoutee a chaque tache terminee.
- [ ] Les resultats convergent avec ceux de l'analyseur Scala en Phase 6.
- [ ] Les sections du rapport (5.1 a 5.4) ont ete remplies a partir de ce carnet.
