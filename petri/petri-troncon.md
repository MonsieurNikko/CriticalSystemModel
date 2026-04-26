# Reseau de Petri - Troncon partage M14

> Livrable L3 du cahier des charges. Modele formel minimal du controle d'acces concurrent de 2 trains a 1 troncon partage.
>
> Ce fichier est la **source de verite du vocabulaire** : les noms des places et transitions doivent etre repris a l'identique dans le code Scala (acteurs Akka et analyseur Petri) et dans les tests. Toute divergence de nommage est un bug de coherence.

---

## 1) Vue d'ensemble

Le reseau modelise le mecanisme d'exclusion mutuelle decrit dans `documentation/contexte/recadrage-m14-troncon-critique.md`. Il est volontairement compact (7 places, 6 transitions) pour rester analysable a la main et par un analyseur Scala maison (livrable L5).

**Scope strict :**
- 2 trains : T1, T2
- 1 troncon partage
- 1 controleur centralise (implicite : il est porte par les transitions, pas par une place dediee)
- Aucune notion de panne, de famine prolongee, ni de priorite

---

## 2) Places (7)

| Place | Signification | Marquage initial |
|-------|---------------|-----------------:|
| `Troncon_libre`     | Le troncon partage est libre, aucun train dessus | 1 |
| `T1_hors`           | Train 1 est en dehors de la zone d'approche      | 1 |
| `T1_attente`        | Train 1 a demande l'acces et attend une reponse  | 0 |
| `T1_sur_troncon`    | Train 1 occupe le troncon partage                | 0 |
| `T2_hors`           | Train 2 est en dehors de la zone d'approche      | 1 |
| `T2_attente`        | Train 2 a demande l'acces et attend une reponse  | 0 |
| `T2_sur_troncon`    | Train 2 occupe le troncon partage                | 0 |

Marquage initial **M0** :
```
M0 = (Troncon_libre=1, T1_hors=1, T1_attente=0, T1_sur_troncon=0,
                       T2_hors=1, T2_attente=0, T2_sur_troncon=0)
```

---

## 3) Transitions (6)

Notation : `pre -> post` signifie consommation d'1 jeton dans chaque place de `pre`, production d'1 jeton dans chaque place de `post`.

| Transition | Pre (consomme) | Post (produit) | Sens metier |
|------------|----------------|----------------|-------------|
| `T1_demande`              | `T1_hors`                          | `T1_attente`                        | Train 1 envoie une demande au controleur |
| `T2_demande`              | `T2_hors`                          | `T2_attente`                        | Train 2 envoie une demande au controleur |
| `T1_entree_autorisee`     | `T1_attente`, `Troncon_libre`      | `T1_sur_troncon`                    | Le controleur autorise Train 1 a entrer |
| `T2_entree_autorisee`     | `T2_attente`, `Troncon_libre`      | `T2_sur_troncon`                    | Le controleur autorise Train 2 a entrer |
| `T1_sortie_liberation`    | `T1_sur_troncon`                   | `T1_hors`, `Troncon_libre`          | Train 1 sort et libere la ressource |
| `T2_sortie_liberation`    | `T2_sur_troncon`                   | `T2_hors`, `Troncon_libre`          | Train 2 sort et libere la ressource |

---

## 4) Schema ASCII

```
                 +--------------------+
                 |   Troncon_libre    |  <-- ressource partagee
                 +--------------------+
                    ^      |      ^
   T1_sortie_      /       |       \      T2_sortie_
   liberation     /        |        \     liberation
                 /         |         \
                /  T1_     |  T2_     \
               /  entree_  |  entree_  \
              /  autorisee | autorisee  \
             /             |             \
   +------------+    +-----+-----+    +------------+
   | T1_hors    |    |           |    | T2_hors    |
   +------------+    |           |    +------------+
        |            |           |          |
   T1_demande        |           |     T2_demande
        v            |           |          v
   +------------+    |           |    +------------+
   | T1_attente |--->|           |<---| T2_attente |
   +------------+    |           |    +------------+
                     v           v
              +----------------+  +----------------+
              | T1_sur_troncon |  | T2_sur_troncon |
              +----------------+  +----------------+
```

Lecture : un train est toujours dans **exactement une** des trois places de sa colonne (`Ti_hors`, `Ti_attente`, `Ti_sur_troncon`). Le `Troncon_libre` est partage entre les deux colonnes.

---

## 5) Invariant principal (a prouver)

```
M(T1_sur_troncon) + M(T2_sur_troncon) + M(Troncon_libre) = 1
```

Pour tout marquage atteignable M depuis M0. Cet invariant est la pierre angulaire de la preuve d'exclusion mutuelle.

**Verification sur M0** : `0 + 0 + 1 = 1`. OK.

**Preservation par chaque transition** :

| Transition | Effet sur la somme `T1_sur_troncon + T2_sur_troncon + Troncon_libre` |
|------------|----------------------------------------------------------------------|
| `T1_demande`           | aucun jeton sur les 3 places concernees : somme inchangee          |
| `T2_demande`           | aucun jeton sur les 3 places concernees : somme inchangee          |
| `T1_entree_autorisee`  | -1 sur `Troncon_libre`, +1 sur `T1_sur_troncon` : somme inchangee  |
| `T2_entree_autorisee`  | -1 sur `Troncon_libre`, +1 sur `T2_sur_troncon` : somme inchangee  |
| `T1_sortie_liberation` | -1 sur `T1_sur_troncon`, +1 sur `Troncon_libre` : somme inchangee  |
| `T2_sortie_liberation` | -1 sur `T2_sur_troncon`, +1 sur `Troncon_libre` : somme inchangee  |

L'invariant est preserve par toutes les transitions, donc il tient sur tout marquage atteignable.

**Consequence directe** : `T1_sur_troncon` et `T2_sur_troncon` ne peuvent jamais valoir 1 simultanement (sinon la somme depasserait 1). C'est l'exclusion mutuelle.

---

## 6) Invariants secondaires (par train)

Pour chaque train i parmi {1, 2} :

```
M(Ti_hors) + M(Ti_attente) + M(Ti_sur_troncon) = 1
```

Un train est toujours dans exactement un de ses trois etats. Verifie sur M0 et preserve par les 3 transitions du train i (chaque transition deplace un jeton entre deux places de la colonne).

---

## 7) Espace d'etats attendu

Depuis M0, l'espace d'etats accessible est de **taille bornee** (6 a 9 marquages selon l'ordre d'exploration). Cette borne sera confirmee programmatiquement par l'analyseur (livrable L5, Phase 6).

Etats attendus a la main :
1. M0 = `(libre, T1_hors, T2_hors)` (notation compacte : seules les places marquees a 1)
2. `(libre, T1_attente, T2_hors)` apres `T1_demande`
3. `(libre, T1_hors, T2_attente)` apres `T2_demande`
4. `(libre, T1_attente, T2_attente)` les deux ont demande
5. `(T1_sur_troncon, T2_hors)` T1 est entre depuis l'etat 2
6. `(T1_sur_troncon, T2_attente)` T1 est entre alors que T2 attendait
7. `(T2_sur_troncon, T1_hors)` symetrique
8. `(T2_sur_troncon, T1_attente)` symetrique

Aucun de ces marquages ne contient simultanement un jeton dans `T1_sur_troncon` et `T2_sur_troncon` : l'exclusion mutuelle est respectee.

---

## 8) Correspondance avec les messages Akka

Cette table sera reprise et raffinee dans `documentation/livrables/comparaison.md` (livrable L6). Premiere ebauche :

| Transition Petri          | Message Akka declencheur                       | Acteur emetteur -> recepteur     |
|---------------------------|------------------------------------------------|----------------------------------|
| `T1_demande`              | `Demande(Train1, replyTo)`                     | Train1 -> SectionController      |
| `T2_demande`              | `Demande(Train2, replyTo)`                     | Train2 -> SectionController      |
| `T1_entree_autorisee`     | `Autorisation` (en reponse a `Demande(Train1)`)| SectionController -> Train1      |
| `T2_entree_autorisee`     | `Autorisation` (en reponse a `Demande(Train2)`)| SectionController -> Train2      |
| `T1_sortie_liberation`    | `Sortie(Train1)`                               | Train1 -> SectionController      |
| `T2_sortie_liberation`    | `Sortie(Train2)`                               | Train2 -> SectionController      |

Le message `Attente` (refus temporaire envoye par le controleur) **n'a pas de transition Petri associee** : c'est une notification de protocole, pas un changement d'etat formel. Le train reste dans `Ti_attente` jusqu'a recevoir une `Autorisation`. Cette dissymetrie est documentee et assumee.

---

## 9) Limites assumees

- **Pas de fairness explicite** : si T1 demande en boucle juste apres avoir libere, T2 peut attendre indefiniment dans le modele Petri pur. La preuve de Liveness suppose une hypothese d'arbitrage non pathologique du controleur (FIFO en pratique cote Akka). Cette hypothese sera formalisee dans le rapport (L4).
- **Pas de notion de temps** : le modele est purement combinatoire, aucune duree n'est attachee aux transitions.
- **Pas de pannes** : aucune transition ne modelise la perte de message, le crash d'acteur, ou le timeout. C'est un choix de scope, pas un oubli.

---

## 10) Verification a faire avec l'analyseur (Phases 5-6)

L'analyseur Scala doit confirmer programmatiquement :
- [ ] Tous les marquages atteignables verifient l'invariant principal.
- [ ] Tous les marquages atteignables verifient les invariants par train.
- [ ] Aucun marquage atteignable n'est un deadlock (au moins une transition tirable depuis chaque etat).
- [ ] Le nombre de marquages atteignables est borne et coherent avec la liste de la section 7.
