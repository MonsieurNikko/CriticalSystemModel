# Comparaison Akka vs Reseau de Petri - 3 scenarios critiques (modele etendu PSD)

> Livrable L6 du cahier des charges. Mise en correspondance ligne a ligne entre la simulation Akka et les transitions du reseau de Petri sur les 3 scenarios retenus apres extension PSD du 29/04/2026.
>
> **Source de verite du vocabulaire** : `petri/petri-troncon.md` (12 places, 12 transitions effectives) et `documentation/gouvernance/lexique.md` (4 etats par train, 8 messages cote controleurs/portes + 4 messages cote trains).
>
> Etat : squelette etendu en Phase A (avant code). Sections 5 et 6 a finaliser apres execution de l'analyseur (Phase D).

---

## 1) Methode de comparaison

Pour chaque scenario, on liste l'evenement Akka (envoi/reception de message), on indique la transition Petri correspondante, et on note le marquage Petri resultant. La regle de coherence est : **chaque transition Petri tirable doit avoir un message Akka declencheur identifiable, et reciproquement**, sauf cas explicitement documentes (`Attente`, `PortesOuvertes`, `PortesFermees` qui sont des notifications de protocole).

Notation compacte des marquages : on liste uniquement les places ayant un jeton, separees par virgules.

Marquage initial M0 (5 jetons) :
```
M0 = (Canton_libre, Quai_libre, Portes_fermees, T1_hors, T2_hors)
```

---

## 2) Scenario 1 - Cycle nominal complet (un train, canton + quai + portes)

**Description metier** : un seul train demande l'acces au canton, l'obtient, traverse, arrive a quai, ouvre les portes, ferme les portes, repart. Cycle complet sans concurrence.

**Conditions initiales** : M0.

| Etape | Evenement Akka                                                                 | Transition Petri          | Marquage resultant                                                              |
|------:|--------------------------------------------------------------------------------|---------------------------|---------------------------------------------------------------------------------|
| 1     | Train1 envoie `Demande(Train1, replyTo)` au SectionController                  | `T1_demande`              | `(Canton_libre, Quai_libre, Portes_fermees, T1_attente, T2_hors)`               |
| 2     | SectionController repond `Autorisation` a Train1                               | `T1_entree_canton`        | `(Quai_libre, Portes_fermees, T1_sur_canton, T2_hors)`                          |
| 3     | Train1 envoie `Sortie(Train1)` au SectionController + `ArriveeQuai(Train1, replyTo)` au QuaiController, puis QuaiController repond `Autorisation` | `T1_arrivee_quai` | `(Canton_libre, Portes_fermees, T1_a_quai, T2_hors)`                            |
| 4     | Train1 envoie `OuverturePortes(Train1)` au GestionnairePortes, qui repond `PortesOuvertes` | `Ouverture_portes_T1`     | `(Canton_libre, Portes_ouvertes, T1_a_quai, T2_hors)`                           |
| 5     | Train1 envoie `FermeturePortes(Train1)` au GestionnairePortes, qui repond `PortesFermees` | `Fermeture_portes_T1`     | `(Canton_libre, Portes_fermees, T1_a_quai, T2_hors)`                            |
| 6     | Train1 envoie `DepartQuai(Train1)` au QuaiController                           | `T1_depart_quai`          | `(Canton_libre, Quai_libre, Portes_fermees, T1_hors, T2_hors)` (= M0)           |

**Verifications cle** :
- A l'etape 4 : `Portes_ouvertes=1` ET `T1_a_quai=1` -> invariant PSD-Open OK.
- A l'etape 6 : `T1_depart_quai` n'est tirable que parce que `Portes_fermees=1` -> invariant PSD-Departure OK.
- Marquage final = M0 : le systeme revient a l'etat initial, le cycle est ferme.

---

## 3) Scenario 2 - Concurrence canton + quai

**Description metier** : Train1 entre dans le canton et avance jusqu'a quai. Pendant ce temps Train2 arrive, demande, et obtient le canton (libere par Train1 quand il atteint le quai). On verifie qu'il y a bien tuilage, qu'aucun marquage n'a 2 trains au meme endroit, et que les portes restent fermees pour Train2 puisqu'il n'est pas encore a quai.

**Conditions initiales** : M0.

| Etape | Evenement Akka                                                                 | Transition Petri          | Marquage resultant                                                                          |
|------:|--------------------------------------------------------------------------------|---------------------------|---------------------------------------------------------------------------------------------|
| 1     | Train1 envoie `Demande(Train1, ...)`                                           | `T1_demande`              | `(Canton_libre, Quai_libre, Portes_fermees, T1_attente, T2_hors)`                           |
| 2     | SectionController repond `Autorisation` a Train1                               | `T1_entree_canton`        | `(Quai_libre, Portes_fermees, T1_sur_canton, T2_hors)`                                      |
| 3     | Train2 envoie `Demande(Train2, ...)` -> SectionController repond `Attente`     | `T2_demande`              | `(Quai_libre, Portes_fermees, T1_sur_canton, T2_attente)`                                   |
| 4     | Train1 atteint le quai : `Sortie` au SectionController + `ArriveeQuai` au QuaiController + `Autorisation` retour | `T1_arrivee_quai` | `(Canton_libre, Portes_fermees, T1_a_quai, T2_attente)`                                     |
| 5     | SectionController depile la file FIFO et envoie `Autorisation` a Train2        | `T2_entree_canton`        | `(Portes_fermees, T1_a_quai, T2_sur_canton)`                                                |
| 6     | Train1 envoie `OuverturePortes(Train1)` -> `PortesOuvertes`                    | `Ouverture_portes_T1`     | `(Portes_ouvertes, T1_a_quai, T2_sur_canton)`                                               |
| 7     | Train1 envoie `FermeturePortes(Train1)` -> `PortesFermees`                     | `Fermeture_portes_T1`     | `(Portes_fermees, T1_a_quai, T2_sur_canton)`                                                |
| 8     | Train1 envoie `DepartQuai(Train1)` au QuaiController                           | `T1_depart_quai`          | `(Quai_libre, Portes_fermees, T1_hors, T2_sur_canton)`                                      |
| 9     | Train2 envoie `Sortie` + `ArriveeQuai(Train2, ...)` -> `Autorisation` retour   | `T2_arrivee_quai`         | `(Canton_libre, Portes_fermees, T1_hors, T2_a_quai)`                                        |

**Verifications cle** :
- A toutes les etapes : `T1_sur_canton + T2_sur_canton + Canton_libre = 1` (exclusion canton).
- A toutes les etapes : `T1_a_quai + T2_a_quai + Quai_libre = 1` (exclusion quai).
- A l'etape 6 : `Portes_ouvertes=1` -> seul T1 est a quai (T2 sur canton). PSD-Open OK.
- A l'etape 8 : `T1_depart_quai` tirable car `Portes_fermees=1`. PSD-Departure OK.
- Au moment ou Train2 occupe le canton (etapes 5-8), Train1 est a quai et les portes peuvent etre ouvertes : aucune interference, les deux ressources sont independantes.

**Note sur le determinisme** : cote Akka, l'ordre est garanti par les files FIFO des deux controleurs (Section + Quai). Cote Petri, depuis le marquage de l'etape 4, plusieurs transitions sont tirables : `T2_entree_canton`, `Ouverture_portes_T1`. L'analyseur explore les deux branches.

---

## 4) Scenario 3 - Surete PSD (tentative invalide rejetee)

**Description metier** : on cherche a verifier que la garde de surete des portes est **effective**. On simule une demande d'ouverture de portes alors qu'aucun train n'est a quai. Le `GestionnairePortes` doit refuser. Cote Petri, on verifie qu'aucune transition `Ouverture_portes_Ti` n'est tirable depuis un marquage ou les deux trains sont hors quai.

**Conditions initiales** : M0 (aucun train a quai).

### 4.1 Cas Akka : tentative directe et rejet

| Etape | Evenement Akka                                                                 | Transition Petri attendue       | Resultat                                                                          |
|------:|--------------------------------------------------------------------------------|---------------------------------|-----------------------------------------------------------------------------------|
| 1     | Un acteur envoie `OuverturePortes(Train1)` au GestionnairePortes alors que Train1 est `hors` | (aucune)                        | GestionnairePortes ignore (cas defensif documente, pas de message en retour)      |

**Verification cote tests** : `GestionnairePortesSpec` doit explicitement coder ce cas (`given an OuverturePortes message when no train is at quai, then state stays portesFermees, no PortesOuvertes is sent`).

### 4.2 Cas Petri : analyse de tirabilite

Depuis M0, les transitions tirables sont **uniquement** `T1_demande` et `T2_demande`. La transition `Ouverture_portes_T1` exige `Portes_fermees=1` (OK) ET `T1_a_quai=1` (KO car `T1_a_quai=0` dans M0). Donc `Ouverture_portes_T1` n'est **pas** tirable. Idem pour T2.

**L'analyseur Scala doit produire** :
- Pour M0 : enumeration des transitions tirables = `{T1_demande, T2_demande}`. Si l'analyseur signale `Ouverture_portes_Ti` comme tirable depuis M0, c'est un bug du modele (cas D du protocole-coordination).

### 4.3 Cas Petri : verification globale de la propriete PSD-Open

Pour chaque marquage M atteignable depuis M0, l'analyseur verifie :
```
M(Portes_ouvertes) = 1  =>  M(T1_a_quai) + M(T2_a_quai) = 1
```

Une violation = bug du modele. Une absence de violation sur les ~15-18 marquages = preuve programmatique de la surete PSD-Open.

**Verifications cle** :
- Cote Akka : la garde de surete est codee explicitement dans `GestionnairePortes.scala`. Sans cette garde, le code accepterait une ouverture invalide (test rouge).
- Cote Petri : la garde est structurelle (pre-condition de la transition). Sans `Ti_a_quai` dans le pre, le modele accepterait l'ouverture invalide (test rouge de l'analyseur).
- **Convergence des deux niveaux** : le code Akka et le modele Petri implementent la meme contrainte de surete, par deux moyens differents (garde defensive vs pre-condition de transition).

---

## 5) Synthese des transitions couvertes par les 3 scenarios

| Transition Petri          | Couverte par scenario |
|---------------------------|-----------------------|
| `T1_demande`              | 1, 2                  |
| `T2_demande`              | 2                     |
| `T1_entree_canton`        | 1, 2                  |
| `T2_entree_canton`        | 2                     |
| `T1_arrivee_quai`         | 1, 2                  |
| `T2_arrivee_quai`         | 2                     |
| `Ouverture_portes_T1`     | 1, 2                  |
| `Ouverture_portes_T2`     | (non couverte explicitement, symetrique) |
| `Fermeture_portes_T1`     | 1, 2                  |
| `Fermeture_portes_T2`     | (non couverte explicitement, symetrique) |
| `T1_depart_quai`          | 1, 2                  |
| `T2_depart_quai`          | (non couverte explicitement, symetrique) |

Les 3 scenarios couvrent **9 transitions sur 12**. Les 3 non couvertes sont strictement symetriques de transitions T1 deja jouees ; aucune nouvelle propriete a verifier. Le scenario 3 verifie en plus la **non-tirabilite** des transitions `Ouverture_portes_Ti` depuis les marquages sans train a quai (preuve par contre-exemple pour la surete PSD).

---

## 6) Resultats de l'analyseur Petri (a completer en Phase D)

Cette section sera remplie quand l'analyseur Scala etendu (livrable L5) sera operationnel. A inserer apres execution de `sbt "runMain m14.petri.Analyseur"` :

- [ ] Sortie listant tous les marquages atteignables (cible : 15-18).
- [ ] Confirmation des **3 invariants de ressource** sur tous les marquages :
  - canton : `T1_sur_canton + T2_sur_canton + Canton_libre = 1`
  - quai : `T1_a_quai + T2_a_quai + Quai_libre = 1`
  - portes : `Portes_fermees + Portes_ouvertes = 1`
- [ ] Confirmation des **2 invariants critiques de surete PSD** :
  - PSD-Open : pour tout M atteignable, `M(Portes_ouvertes)=1 => M(T1_a_quai)+M(T2_a_quai)=1`
  - PSD-Departure : pour tout M atteignable et tout train Ti, si `Ti_depart_quai` tirable depuis M alors `M(Portes_fermees)=1`
- [ ] Confirmation de l'absence de deadlock.
- [ ] Comparaison entre marquages atteints par la simulation Akka (scenarios 1 et 2) et marquages calcules par l'analyseur : doivent coincider exactement.

---

## 7) Limites de la comparaison

- Les **files FIFO Akka** (SectionController + QuaiController) ne sont pas modelisees dans le reseau de Petri. Cela introduit une asymetrie : Akka est plus deterministe que le modele Petri sur le choix du train a servir. C'est volontaire pour garder le reseau compact (cf `petri/petri-troncon.md` section 9).
- Les **timings** ne sont pas captures, ni cote Akka (asynchrone mais sans retard explicite ; pas de `Thread.sleep` dans les tests), ni cote Petri (modele atemporel).
- Le **delai d'embarquement** (montee/descente voyageurs entre `OuverturePortes` et `FermeturePortes`) est encode comme un message immediat dans les tests pour garder le determinisme. En production reelle, ce serait un `context.scheduleOnce`.
- La **garde de surete** des portes est verifiee de deux manieres convergentes mais conceptuellement differentes : garde defensive cote Akka, pre-condition de transition cote Petri. Le rapport (L4 section 5.5) discute cette convergence comme triple preuve (structurelle + inductive + programmatique).
- La comparaison reste **qualitative** (correspondance de transitions et marquages) et non quantitative (pas de mesure de performance, latence, debit).

---

## 8) Annexe historique - scenarios du modele initial (7 places, 6 transitions)

L'enumeration originale a 3 scenarios pour le modele "1 troncon, 7 places" (avant extension PSD du 29/04) couvrait nominal / concurrence / sortie-progression sur 6 transitions. Cette version est conservee dans l'historique git (`documentation/suivi/historique.md` entrees du 27/04) et a servi de base pedagogique avant l'extension. Les noms de transitions de cette version (`T1_entree_autorisee`, `T1_sortie_liberation`) sont **caduques** apres renommage (cf `documentation/gouvernance/lexique.md` section 1).
