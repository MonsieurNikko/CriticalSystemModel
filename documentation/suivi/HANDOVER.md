# HANDOVER - Comment reprendre une tache d'un equipier sans se perdre

> Document destine aux 4 contributeurs du projet CriticalSystemModel.
> Si tu ouvres une branche que tu n'as pas ecrite, commence ici.

---

## 1) Reprendre une tache en 10 minutes

Ordre de lecture obligatoire :

1. **`documentation/suivi/PLAN.md`** - quelle phase est en cours, quel est le livrable cible.
2. **`documentation/suivi/historique.md` (entree la plus recente)** - ce qui a ete fait dans la derniere session, les commandes lancees.
3. **`git log --oneline -15`** sur la branche - historique des commits, en lisant les messages "Quoi / Pourquoi / Comment relire".
4. **Le fichier "comment relire" indique dans le dernier commit** - c'est le point d'entree code de la session.
5. **`sbt compile && sbt test`** - confirme que ton checkout est sain avant de toucher quoi que ce soit.

Si une de ces 5 etapes echoue ou semble incoherente : **stop**, demande a l'auteur de la derniere modification avant de continuer.

---

## 2) Glossaire du projet

> Note historique : les premieres phases utilisaient le vocabulaire "troncon partage". Dans le modele final, ce troncon est precise en **canton de signalisation**, puis on ajoute le quai et les portes palieres (PSD).

### Termes Akka / Scala

- **Acteur** : entite isolee qui recoit des messages et change d'etat. Ne partage rien avec les autres acteurs.
- **`Behavior[T]`** : fonction qui dit comment un acteur reagit aux messages de type T.
- **`ActorRef[T]`** : adresse d'un acteur, on lui envoie des messages avec `actorRef ! message`.
- **`Behaviors.receive`** / **`Behaviors.receiveMessage`** : facons classiques de definir un comportement qui ecoute des messages.
- **Message** : objet immuable envoye d'un acteur a un autre. Dans notre projet : `Demande`, `Autorisation`, `Sortie`, etc.
- **Probe** (TestKit) : faux acteur qu'on cree dans les tests pour intercepter et verifier les messages recus.
- **Typed** : variante moderne d'Akka ou les messages sont types. C'est ce qu'on utilise.

### Termes Reseau de Petri

- **Place** : un "etat" possible. Dans le modele final : `Canton_libre`, `Quai_libre`, `Portes_fermees`, `T1_attente`, `T1_sur_canton`, `T1_a_quai`, etc.
- **Marquage** (marking) : combien de jetons sont dans chaque place a un instant donne. Notre marquage initial final : 1 jeton dans `Canton_libre`, 1 dans `Quai_libre`, 1 dans `Portes_fermees`, 1 dans `T1_hors`, 1 dans `T2_hors`.
- **Transition** : un evenement qui consomme des jetons dans certaines places et en produit dans d'autres. Exemple : `T1_entree_canton` consomme 1 jeton de `Canton_libre` et 1 de `T1_attente`, produit 1 jeton dans `T1_sur_canton`.
- **Tirable** (firable) : une transition est tirable si toutes ses places d'entree ont assez de jetons.
- **Espace d'etats** (state space) : ensemble de tous les marquages atteignables depuis le marquage initial en tirant des transitions.
- **Deadlock** : marquage ou aucune transition n'est tirable et qui n'est pas un etat final voulu.
- **Invariant de place** (P-invariant) : combinaison lineaire de places qui reste constante quoi qu'il arrive. Exemple final : `T1_sur_canton + T2_sur_canton + Canton_libre = 1`.

### Termes verification

- **Safety** (surete) : "rien de mal n'arrive jamais". Exemple : pas de collision sur le canton, portes jamais ouvertes sans train a quai.
- **Liveness** (vivacite) : "quelque chose de bien finit toujours par arriver". Exemple : un train en attente finit par passer.
- **LTL** (Linear Temporal Logic) : logique pour exprimer Safety/Liveness. Operateurs : `G` (toujours), `F` (finalement), `X` (au prochain pas).
- **Fairness** (equite) : hypothese disant qu'un evenement possible finit toujours par arriver. Necessaire pour prouver la Liveness.

### Termes projet

- **Phase 1 a 10** : decoupage du sprint, voir PLAN.md §4.
- **Socle initial** : le sous-systeme 2 trains + 1 troncon partage, utilise pour valider l'exclusion mutuelle de base.
- **Coeur final** : 2 trains + 1 canton + 1 quai + 1 paire de portes palieres. C'est le modele rendu.

---

## 3) Comment lire le code des autres sans paniquer

Quand tu ouvres un fichier ecrit par un coequipier :

1. **Lire les noms de fonctions** uniquement, sans rentrer dans le corps. Tu dois pouvoir reconstituer le scenario juste avec les noms.
2. **Choisir UNE fonction** et la lire entierement. Si tu ne comprends pas en 2 minutes, le code est trop complexe : ouvre une issue ou demande a l'auteur.
3. **Lancer le test correspondant** pour voir le comportement en action.

Conventions du repo :
- Pas d'implicits, pas de macros, pas de point-free style, pas d'operateurs custom.
- Pas de chaines complexes de `flatMap` / `for-yield` ; preferer `if/else` et boucles simples.
- Variables longues et descriptives (`trainEnAttente` plutot que `t`).
- Pas d'en-tete obligatoire dans chaque fichier source : les noms de packages/classes suffisent. Ajouter seulement quelques commentaires utiles.
- Fonctions courtes (<30 lignes), nesting <=3.
- Commentaires rares, en francais simple, expliquant POURQUOI, jamais QUOI.

Si le code ne respecte pas ces conventions, simplifie ou demande a l'auteur de simplifier.

---

## 4) Checklist avant de pousser

A faire dans cet ordre :

- [ ] **Test de defendabilite** : ferme ton editeur, ouvre le fichier modifie, explique-le a voix haute en 2 min. Si tu n'y arrives pas, simplifie le code avant de pousser.
- [ ] **Commentaires sobres** : pas de pavé explicatif dans le code, seulement quelques notes utiles quand le comportement n'est pas evident.
- [ ] **`sbt compile`** : OK.
- [ ] **`sbt test`** : OK (ou echec documente).
- [ ] **`documentation/suivi/historique.md`** : nouvelle entree decrivant les changements, les commandes, le resultat.
- [ ] **Message de commit** structure : 1) Quoi, 2) Pourquoi, 3) Comment relire (quel fichier ouvrir en premier).
- [ ] **Pas d'artefact** dans le diff (`target/`, `.bsp/`, `.metals/`...).
- [ ] **Branche dediee**, jamais `main` directement.

Si une seule case n'est pas cochee : tu ne pousses pas. Tu corriges ou tu demandes a un coequipier.

---

## 5) Anti-patterns (pieges typiques)

| Symptome | Probleme | Que faire |
|----------|----------|-----------|
| Code "trop dense" pour ton niveau Scala | Surchargé | Reecrire en version naive (if/else, vals nommes) |
| Implicits, type classes, tagless final | Style trop avance pour le contexte | Refuser, redemander en if/else simples |
| Une fonction de 80 lignes | Trop dense | Decouper en 2-3 fonctions nommees |
| Pas de tests pour le code ajoute | Defendabilite zero | Ajouter au moins 1 test heureux |
| Commit "wip" ou "fix stuff" | Tracabilite cassee | Re-rediger : Quoi / Pourquoi / Comment relire |
| `historique.md` non mis a jour | Violation des regles projet | Bloquer le push, ajouter l'entree |
| Une seule personne peut expliquer le code | Echec d'equipe | Reprendre en pair pour transferer la connaissance |

---

## 6) Demande d'aide

Si, malgre ce document, tu te perds :

1. Lis l'entree la plus recente de `documentation/suivi/historique.md` une 2eme fois.
2. Pose une question dans le canal d'equipe avec le commit hash et le fichier qui te bloque.
3. L'auteur du dernier changement doit te repondre dans la journee pendant le sprint.

**Important** : poser une question n'est jamais un echec. Pousser du code qu'on ne comprend pas, c'est un echec.
