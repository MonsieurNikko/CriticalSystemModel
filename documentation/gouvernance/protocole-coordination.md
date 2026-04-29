# Protocole de coordination - Travail parallele code / preuves manuelles

> **Objet** : standardiser la collaboration entre les contributeurs qui codent (Piste B) et ceux qui font les preuves manuelles (Piste A), en anticipant les conflits qui surgissent pendant le codage.
>
> **A qui s'adresse ce document** : aux 4 contributeurs du projet, sans exception. A lire en debut de chaque session de travail.
>
> **Regle d'or** : si les deux pistes divergent, on **stoppe** les deux et on synchronise. On ne pousse pas du code qui contredit le carnet de preuves, et on ne valide pas une preuve qui ne correspond plus au code.

---

## 1) Sources de verite (artefacts canoniques)

Chaque information du projet a **une seule** source de verite. En cas de conflit, ce tableau dit qui gagne.

| Information                                | Source de verite                                                | Qui edite                     | Qui doit relire             |
|--------------------------------------------|-----------------------------------------------------------------|-------------------------------|-----------------------------|
| Modele formel (places, transitions, PSD)   | `petri/petri-troncon.md`                                        | Piste A                       | Toute l'equipe              |
| Vocabulaire metier <-> code <-> Petri      | `documentation/gouvernance/lexique.md`                          | Piste A                       | Piste B avant chaque commit |
| Protocole de messages Akka (8+4 messages)  | `src/main/scala/m14/troncon/Protocol.scala`                     | Piste B                       | Piste A                     |
| Arbitrage du canton                        | `src/main/scala/m14/troncon/SectionController.scala`            | Piste B                       | Piste A pour validation FIFO|
| Arbitrage du quai (NEW)                    | `src/main/scala/m14/troncon/QuaiController.scala`               | Piste B                       | Piste A pour validation FIFO|
| Garde de surete des portes (NEW, CRITIQUE) | `src/main/scala/m14/troncon/GestionnairePortes.scala`           | Piste B                       | Piste A obligatoire         |
| Machine a etats du train (4 etats)         | `src/main/scala/m14/troncon/Train.scala`                        | Piste B                       | Piste A                     |
| Preuves d'invariants a la main             | `documentation/livrables/preuves-manuelles.md`                  | Piste A                       | Piste B en Phase D          |
| Correspondance scenarios <-> transitions   | `documentation/livrables/comparaison.md`                        | Piste A + Piste B (co-redige) | Toute l'equipe              |
| Sortie programmatique de l'analyseur       | `src/main/scala/m14/petri/` + log                               | Piste B                       | Piste A en Phase D          |
| Rapport final                              | `documentation/livrables/rapport.md`                            | Toute l'equipe                | Toute l'equipe              |

**Regle** : si tu modifies un fichier, tu es responsable de notifier les autres editeurs concernes via une entree dans `documentation/suivi/historique.md` **dans la meme session**.

---

## 2) Liste gelee - Ce qui NE change PAS sans reunion d'equipe

Les decisions suivantes sont verrouillees apres l'extension PSD du 29 avril 2026. Toute modification necessite un accord explicite des 4 contributeurs (a tracer dans historique.md).

- [LOCKED] **12 places, 12 transitions** dans le reseau de Petri etendu (canton + quai + portes). Pas plus, pas moins.
- [LOCKED] **8 messages Akka cote controleurs** : `Demande`, `Sortie`, `ArriveeQuai`, `DepartQuai`, `OuverturePortes`, `FermeturePortes`, plus `Autorisation`, `Attente`, `PortesOuvertes`, `PortesFermees` cote trains. Aucun nouveau message.
- [LOCKED] **2 trains** dans le scope. Pas de generalisation a N trains.
- [LOCKED] **Marquage initial M0** : `Canton_libre=1, Quai_libre=1, Portes_fermees=1, T1_hors=1, T2_hors=1`. Toute autre place a 0. 5 jetons en circulation.
- [LOCKED] **3 invariants de ressource** :
  - canton : `T1_sur_canton + T2_sur_canton + Canton_libre = 1`
  - quai : `T1_a_quai + T2_a_quai + Quai_libre = 1`
  - portes : `Portes_fermees + Portes_ouvertes = 1`
- [LOCKED] **2 invariants critiques de surete PSD** :
  - PSD-Open : `Portes_ouvertes = 1 => T1_a_quai + T2_a_quai = 1`
  - PSD-Departure : `Ti_depart_quai tirable => Portes_fermees = 1`
- [LOCKED] **3 scenarios de validation** : Nominal cycle complet, Concurrence canton+quai, Surete PSD (tentative invalide). Aucun 4e scenario sans accord.

**Decision archivee 26/04** : verrouillage initial a 7 places / 6 transitions / 4 messages, abandonne le 29/04 lors de l'extension PSD (cf historique.md). Cette extension a ete validee par l'utilisateur comme moyen de rendre le projet plus M14-realiste.

Si pendant le codage une de ces verrous semble bloquer, **stop** : on en discute avant de toucher quoi que ce soit. Probablement le code peut s'adapter sans casser le verrou.

---

## 3) Procedures de changement par cas

### Cas A - Renommer un terme (ex : `comportementHors` -> `etatHors`)

1. Modifier `documentation/gouvernance/lexique.md` en premier (ajouter une note "ancien -> nouveau").
2. Modifier `petri/petri-troncon.md` si la place Petri est concernee.
3. Modifier le code Scala.
4. Mettre a jour `documentation/livrables/preuves-manuelles.md` (recherche-remplace).
5. Une entree historique.md citant les 4 fichiers.

**Personne en charge** : la piste qui detecte le besoin propose, l'autre piste relit avant le push.

### Cas B - Ajouter un detail de protocole non couvert

Exemple : "que fait le controleur si un train envoie `Sortie` alors qu'il n'est pas sur le troncon ?"

1. Verifier d'abord si le cas est **possible** dans le modele Petri. Si la transition n'est pas tirable depuis aucun marquage atteignable, c'est un cas qui ne peut pas se produire avec une implementation correcte.
2. Si c'est le cas (cas impossible) : dans le code, on **ignore le message** avec un commentaire explicite : `// cas impossible si la machine d'etat est respectee, robustesse defensive`.
3. Ne **pas** ajouter de transition Petri pour modeliser ce cas. Le modele Petri reflete le comportement attendu, pas les bugs.
4. Documenter le choix dans `documentation/livrables/preuves-manuelles.md` section "Robustesse defensive".

### Cas C - Le code revele un bug du modele Petri

Exemple : le code montre qu'une transition cense etre tirable ne l'est pas dans certains marquages.

1. **Stop** sur la piste B. Ne pas committer.
2. Reunion ou message a l'equipe.
3. Si le bug est dans le modele Petri : corriger `petri/petri-troncon.md`, recalculer la tache 1 du carnet, propager.
4. Si le bug est dans le code : corriger le code, le modele Petri reste la reference.
5. Tracer la decision dans historique.md avec justification.

### Cas D - Le carnet de preuves revele une incoherence dans le code

Symetrique du cas C. Meme procedure : stop, reunion, decision, trace.

### Cas E - On veut ajouter un test scenario en plus des 3 retenus

C'est une violation du verrou n°6. Pas autorise sans reunion. Si vraiment necessaire :
1. Justifier pourquoi les 3 scenarios sont insuffisants.
2. Mettre a jour `documentation/contexte/recadrage-m14-troncon-critique.md` section 7.
3. Mettre a jour `documentation/livrables/preuves-manuelles.md` et `documentation/livrables/comparaison.md`.
4. Tracer dans historique.md avec validation des 4 contributeurs.

---

## 4) Changements anticipes pendant le codage Phase 2-3

Voici les questions concretes qui vont **probablement** se poser pendant l'implementation, et la reponse de reference. **Lire avant de commencer Phase 2.**

### Q1 - Quel type de file pour la FIFO du controleur ?

**Reponse** : `scala.collection.immutable.Queue[IdTrain]`. Choix interne, aucun impact sur Petri ou lexique.

### Q2 - Que faire si un train envoie deux `Demande` consecutives ?

**Reponse** : impossible si la machine d'etat du train est correcte (un train en `attente` ou `sur_troncon` n'envoie pas de nouvelle `Demande`). Cote controleur : ignorer la deuxieme demande avec commentaire defensif. Pas de modification du modele Petri.

### Q3 - Que faire si un train envoie `Sortie` alors qu'il n'occupe pas le troncon ?

**Reponse** : ignorer cote controleur (commentaire defensif). Cas impossible par construction. Pas de transition Petri ajoutee.

### Q4 - Le `Train` doit-il garder une reference vers le `SectionController` ?

**Reponse** : oui, passee au constructeur (`def apply(id: IdTrain, controleur: ActorRef[...])`). Conforme au squelette deja en place. Aucun changement de Petri ni de lexique.

### Q5 - Comment representer l'etat interne du `SectionController` ?

**Reponse** : par un changement de `Behavior` (un Behavior par etat interne, conforme au lexique section 2). Pas de variable mutable. Pas de `var`. Le passage d'etat se fait par retourner un nouveau `Behavior`.

### Q6 - Comment tester la file FIFO sans casser l'invariant Petri ?

**Reponse** : la FIFO est un detail d'implementation Akka. Le test verifie que sur le scenario "Concurrence", le **premier** train a demander obtient l'autorisation. Le modele Petri lui ne specifie pas qui gagne (non determinisme). Le rapport documente cette dissymetrie.

### Q7 - Quel timeout pour `expectMessage` dans les tests ?

**Reponse** : 1 seconde maximum. Si un message met plus, c'est un bug. Pas de `Thread.sleep`. Probes deterministes.

### Q8 - Le `StationControl.scala` existant doit-il etre modifie ? [CADUQUE]

**Reponse historique (Phase 2)** : non, hors scope, garder tel quel.

**Mise a jour 2026-04-27** : `StationControl.scala` et `StationControlSpec.scala` ont ete supprimes du repo (entree historique correspondante du 2026-04-27). Le `Main.scala` demontre desormais les 3 scenarios troncon. Cette question n'a plus d'objet.

### Q9 - Le `Main.scala` doit-il etre adapte pour faire tourner les 3 scenarios ?

**Reponse** : optionnel pour la Phase 2. Suffit que les tests les couvrent. Si on l'adapte en Phase 9, on remplace le contenu de `Main` par une demonstration courte des 3 scenarios.

### Q10 - L'analyseur Petri (Phase 5) doit-il lire le reseau depuis `petri/petri-troncon.md` ?

**Reponse** : non. Le reseau est encode **en dur** dans `src/main/scala/m14/petri/PetriNet.scala` (pas de parser de fichier). Conforme au scope verrou : pas de generalisation. La coherence avec `petri/petri-troncon.md` est verifiee par relecture, pas par parsing.

### Q11 - Le `QuaiController` doit-il etre une copie conforme de `SectionController` ? [NEW Phase 7]

**Reponse** : **oui, deliberement**. Memes etats internes (`quaiLibre`, `quaiOccupe(occupant, file)`), meme logique FIFO, memes cas defensifs. La duplication est assumee : elle simplifie la lecture et evite une abstraction prematuree (regles vibe-code section 12). Si plus tard on factorise, ce sera apres rendu.

### Q12 - Comment le `GestionnairePortes` sait-il qu'un train est a quai ? [NEW Phase 7]

**Reponse** : c'est l'acteur lui-meme qui maintient l'etat. Quand le `Train` arrive a quai (a recu `Autorisation` du `QuaiController`), il envoie `OuverturePortes(idTrain)` au `GestionnairePortes`. Le gestionnaire passe alors de l'etat `portesFermees` a `portesOuvertes(idTrain)`. La garde de surete est : si on recoit `OuverturePortes` alors qu'on est deja en `portesOuvertes(autreTrain)`, on ignore (cas defensif, deux trains a quai en meme temps est exclu par l'invariant quai). 

**Note importante** : le `GestionnairePortes` ne questionne pas le `QuaiController`, il fait confiance a la machine d'etat des trains. La coherence est garantie par le modele Petri (qui empeche `OuverturePortes` sans train a quai).

### Q13 - Le `Train` doit-il faire l'ouverture/fermeture des portes lui-meme ? [NEW Phase 7]

**Reponse** : oui, le train **declenche** l'ouverture en envoyant `OuverturePortes` au gestionnaire des qu'il arrive a quai (recoit `Autorisation` du quai). Apres avoir recu la confirmation `PortesOuvertes`, il attend un delai symbolique (montee/descente voyageurs - en realite quelques secondes), puis envoie `FermeturePortes`. Apres `PortesFermees`, il envoie `DepartQuai` au `QuaiController` et passe a `comportementHors`.

Pour les tests deterministes : le delai d'embarquement est encode comme un message immediat (pas de `Thread.sleep`). En production reelle, ce serait un `context.scheduleOnce`.

### Q14 - Comment la transition Petri `T1_depart_quai` modelise-t-elle "Portes_fermees [read]" ? [NEW Phase 7]

**Reponse** : en Petri ordinaire, on n'a pas de read-arc natif. On emule en consommant `Portes_fermees` puis en le reproduisant (cf petri-troncon.md section 3 : `pre={T1_a_quai, Portes_fermees}, post={T1_hors, Quai_libre, Portes_fermees}`). Net effet : `Portes_fermees` reste a 1 (lecture), mais la transition n'est tirable que si `Portes_fermees=1`. C'est exactement la semantique recherchee.

### Q15 - Quelle preuve donne-t-on de la surete PSD-Open dans le rapport ? [NEW Phase 7]

**Reponse** : trois niveaux de preuve convergents (cf rapport.md section 5.5) :
1. **Structurelle** : la place `Portes_ouvertes` ne peut etre marquee que par les transitions `Ouverture_portes_Ti`, dont le pre contient `Ti_a_quai`. Donc tant que `Portes_ouvertes=1`, un train est a quai.
2. **Inductive** : par recurrence sur le marquage initial et chaque transition (analogue a la preuve de l'invariant principal).
3. **Programmatique** : l'analyseur Scala enumere les marquages atteignables et verifie pour chacun que `Portes_ouvertes=1 => T1_a_quai+T2_a_quai=1`. Si la verification echoue sur un seul marquage, l'analyseur signale une violation (cf `verifierSurteOuverturePortes` dans `Analyseur.scala`).

---

## 5) Points de synchronisation

### Synchronisation quotidienne (5 minutes)

Chaque jour, chaque contributeur poste dans le canal d'equipe (ou commit message) :
- Ce que j'ai fait depuis hier.
- Ce que je touche aujourd'hui.
- Est-ce que ca impacte un fichier de l'autre piste ? (oui/non + lequel)

### Synchronisation de phase

Entre chaque phase du PLAN.md, **5 minutes** pour valider :
- [ ] La phase precedente est terminee selon les criteres "Verification" du PLAN.
- [ ] Les fichiers attendus existent.
- [ ] `historique.md` est a jour.
- [ ] Aucun verrou de la section 2 de ce document n'a ete viole.

### Synchronisation forte en Phase 6 (mercredi 30 avril)

C'est le point de convergence des deux pistes. Apres l'analyseur :

- [ ] Comparer la sortie de l'analyseur avec la tache 1 du carnet (8 marquages attendus).
- [ ] Comparer les invariants verifies par le code avec la tache 2 du carnet.
- [ ] En cas d'ecart : reunion immediate, application de la procedure cas C ou D.
- [ ] Mettre a jour `documentation/livrables/comparaison.md` section 6 avec la sortie brute du code.

---

## 6) Template de demande de changement (RFC court)

Si tu veux modifier quelque chose qui touche les deux pistes, copier-coller ce bloc dans une issue ou un commit avant de modifier :

```
RFC-XXX - Titre court

Auteur : @username (Piste A ou B)
Date : YYYY-MM-DD
Fichiers impactes : liste

Changement propose :
- Avant : ...
- Apres : ...

Motivation :
- ...

Impact sur l'autre piste :
- Carnet (preuves-manuelles.md) : OUI / NON, sections concernees
- Code (Akka ou analyseur) : OUI / NON, fichiers concernes

Validation requise par : @autre-contributeur

Decision : [EN ATTENTE / APPROUVE / REJETE]
```

Aucun changement touchant les verrous (section 2) ne doit etre fait sans cette procedure.

---

## 7) Checklist de cloture de session

A faire avant de quitter une session de travail (5 minutes max) :

- [ ] Mes fichiers compilent (`sbt compile` si j'ai touche du code).
- [ ] Les tests existants passent (`sbt test` si j'ai touche du code).
- [ ] J'ai mis a jour `documentation/suivi/historique.md` avec une entree datee.
- [ ] Si j'ai touche un fichier de la liste section 1, j'ai signale dans la note de session quel autre contributeur doit relire.
- [ ] Si j'ai bloque sur un cas non prevu : j'ai ouvert une RFC (template section 6) plutot que de decider seul.
- [ ] J'ai pousse sur ma branche feature, pas sur main.

---

## 8) Resolution de conflit en cas de blocage

Si les deux pistes sont en desaccord et qu'aucune procedure ci-dessus ne tranche :

1. **Stoppe** ta modification en cours, ne committe pas.
2. Ouvre une issue ou un thread d'equipe avec le contexte (fichiers, lignes, ce qui te bloque).
3. **Source de verite par defaut en cas d'ambiguite** : `petri/petri-troncon.md` (le modele formel l'emporte sur l'implementation).
4. Si la decision necessite plus de 30 minutes de discussion : la reporter en synchronisation de phase suivante, marquer comme `[BLOQUE]` dans historique.md.
5. **Ne jamais** decider seul sur un sujet qui touche les deux pistes.

---

## 9) Anti-patterns (a ne pas faire)

| Anti-pattern | Symptome | Correction |
|--------------|----------|------------|
| "Je code vite, je documente apres" | historique.md vide a la fin de la session | Lien casse vers les regles section 7 du REGLES_PROJET.md |
| "Le carnet n'est pas a jour, je passe en force" | Code incoherent avec preuves manuelles | Stop, appliquer cas C ou D |
| "J'ajoute juste un petit message Akka pour debug" | 5e message dans le protocole | Violation du verrou. Utiliser un log local au lieu d'un message |
| "Je generalise un peu le controleur" | Code qui supporte N trains | Violation du verrou. Hors scope |
| "Je modifie le marquage initial pour tester" | Tests qui ne reflechtent pas la realite | Toujours partir de M0 dans les tests, sinon mention explicite |
| Decision unilaterale sur un sujet bilateral | Conflit detecte tardivement | Synchronisation quotidienne section 5 |

---

## 10) Etat de ce document

- Version : 1.0
- Date de creation : 2026-04-26
- A relire avant Phase 2 par les 4 contributeurs.
- Modifications futures : par RFC uniquement (template section 6).
