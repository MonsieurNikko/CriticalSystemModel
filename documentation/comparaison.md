# Comparaison Akka vs Reseau de Petri - 3 scenarios critiques

> Livrable L6 du cahier des charges. Mise en correspondance ligne a ligne entre la simulation Akka et les transitions du reseau de Petri sur les 3 scenarios retenus.
>
> Etat : squelette cree en Phase 2 (anticipation). A remplir au fil des phases 4 a 6, finalise en Phase 7.

---

## 1) Methode de comparaison

Pour chaque scenario, on liste l'evenement Akka (envoi/reception de message), on indique la transition Petri correspondante, et on note le marquage Petri resultant. La regle de coherence est : **chaque transition Petri tirable doit avoir un message Akka declencheur identifiable, et reciproquement**, sauf cas explicitement documentes (notification `Attente`).

Notation compacte des marquages : on liste uniquement les places ayant un jeton, separees par virgules. Exemple : `(Troncon_libre, T1_hors, T2_hors)` pour M0.

---

## 2) Scenario 1 - Nominal

**Description metier** : un seul train demande l'acces, l'obtient, occupe le troncon, sort, libere la ressource.

**Conditions initiales** : marquage M0 = `(Troncon_libre, T1_hors, T2_hors)`.

| Etape | Evenement Akka                          | Transition Petri          | Marquage resultant                           |
|------:|-----------------------------------------|---------------------------|----------------------------------------------|
| 1     | Train1 envoie `Demande(Train1, ...)`    | `T1_demande`              | `(Troncon_libre, T1_attente, T2_hors)`       |
| 2     | Controleur recoit `Demande` puis envoie `Autorisation` a Train1 | `T1_entree_autorisee` | `(T1_sur_troncon, T2_hors)`     |
| 3     | Train1 envoie `Sortie(Train1)`          | `T1_sortie_liberation`    | `(Troncon_libre, T1_hors, T2_hors)` (= M0)   |

**Verification** : le marquage final est egal a M0. L'invariant principal est respecte a chaque etape (somme des trois places critiques = 1).

---

## 3) Scenario 2 - Concurrence

**Description metier** : les deux trains demandent quasi-simultanement, le controleur en autorise un, l'autre attend.

**Conditions initiales** : M0.

| Etape | Evenement Akka                          | Transition Petri          | Marquage resultant                           |
|------:|-----------------------------------------|---------------------------|----------------------------------------------|
| 1     | Train1 envoie `Demande(Train1, ...)`    | `T1_demande`              | `(Troncon_libre, T1_attente, T2_hors)`       |
| 2     | Train2 envoie `Demande(Train2, ...)`    | `T2_demande`              | `(Troncon_libre, T1_attente, T2_attente)`    |
| 3     | Controleur autorise Train1 (FIFO), envoie `Autorisation` a Train1 et `Attente` a Train2 | `T1_entree_autorisee` (`Attente` n'a pas de transition Petri) | `(T1_sur_troncon, T2_attente)` |

**Verification** : `T1_sur_troncon` et `T2_sur_troncon` ne sont jamais simultanement marques. L'exclusion mutuelle tient.

**Note sur le determinisme** : cote Akka, l'ordre est garanti par la file FIFO du controleur. Cote Petri, les deux transitions `T1_entree_autorisee` et `T2_entree_autorisee` seraient toutes deux tirables a l'etape 3 (non determinisme). L'analyseur Petri doit explorer les deux branches.

---

## 4) Scenario 3 - Sortie / liberation / progression

**Description metier** : un train occupe le troncon, l'autre attend, le premier sort, le second progresse.

**Conditions initiales** : marquage `(T1_sur_troncon, T2_attente)` (etat final du scenario 2).

| Etape | Evenement Akka                          | Transition Petri          | Marquage resultant                           |
|------:|-----------------------------------------|---------------------------|----------------------------------------------|
| 1     | Train1 envoie `Sortie(Train1)`          | `T1_sortie_liberation`    | `(Troncon_libre, T1_hors, T2_attente)`       |
| 2     | Controleur depile la file FIFO et envoie `Autorisation` a Train2 | `T2_entree_autorisee` | `(T2_sur_troncon, T1_hors)` |

**Verification** : le train qui etait en attente progresse effectivement. Pas de blocage. Liveness verifiee sur ce scenario (sous hypothese que le controleur ne rejette pas Train2 indefiniment, ce qui est garanti par la FIFO Akka).

---

## 5) Synthese des transitions couvertes

| Transition Petri          | Couverte par scenario |
|---------------------------|-----------------------|
| `T1_demande`              | 1, 2                  |
| `T2_demande`              | 2                     |
| `T1_entree_autorisee`     | 1, 2                  |
| `T2_entree_autorisee`     | 3                     |
| `T1_sortie_liberation`    | 1, 3                  |
| `T2_sortie_liberation`    | (non couverte par les 3 scenarios, symetrique de `T1_sortie_liberation`) |

Les 3 scenarios couvrent 5 transitions sur 6. La sixieme est symetrique et n'apporte pas de nouveau cas de preuve.

---

## 6) Resultats de l'analyseur Petri (a completer en Phase 6)

Cette section sera remplie quand l'analyseur Scala (livrable L5) sera operationnel.

A inserer :
- [ ] Sortie de l'analyseur listant tous les marquages atteignables.
- [ ] Confirmation de l'invariant principal sur tous les marquages.
- [ ] Confirmation de l'absence de deadlock.
- [ ] Comparaison entre marquages atteints par la simulation Akka et marquages calcules par l'analyseur.

---

## 7) Limites de la comparaison

- La file FIFO Akka **n'est pas modelisee** dans le reseau de Petri. Cela introduit une asymetrie : Akka est plus deterministe que le modele Petri. C'est volontaire pour garder le reseau compact (livrable L3 section 9).
- Les timings et les delais reseau ne sont pas captures, ni cote Akka (asynchrone mais sans retard explicite), ni cote Petri (modele atemporel).
- La comparaison est qualitative (correspondance de transitions) et non quantitative (pas de mesure de performance).
