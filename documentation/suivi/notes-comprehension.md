# Notes de compréhension du code

> **Archive du modele initial** : cette note explique l'ancien `Train` du socle 2 trains / 1 troncon. Le code final a ete upgrade avec `QuaiController` et `GestionnairePortes`. Pour la sequence actuelle, lire `Train.scala`, `START-ICI.md` et `petri/petri-troncon.md`.

## Workflow : spawn → apply → comportementHors

**Fichiers concernés :**
- `src/test/scala/m14/troncon/TrainSpec.scala` (ligne 16)
- `src/main/scala/m14/troncon/Train.scala`

```
spawn( Train(Train1, probeControleur.ref) )
         │
         └─→ Train.apply(Train1, probeControleur.ref)        ← Train.scala L15
                  │
                  └─→ comportementHors(Train1, controleur)   ← Train.scala L21
                           │
                           └─→ Behaviors.setup { context =>
                                    controleur ! Demande(Train1, context.self)  ← envoie immédiatement
                                    comportementEnAttente(Train1, controleur)   ← état suivant
                               }
                                        │
                                        └─→ comportementEnAttente(Train1, controleur)   ← Train.scala L31
                                                 │
                                                 └─→ Behaviors.receiveMessage {
                                                          case Autorisation =>
                                                              comportementSurTroncon(...)  ← entre sur le tronçon
                                                          case Attente =>
                                                              Behaviors.same               ← reste en attente, rien ne se passe
                                                     }
                                                          │ (si Autorisation reçu)
                                                          └─→ comportementSurTroncon(Train1, controleur)   ← Train.scala L45
                                                                   │
                                                                   └─→ Behaviors.setup { context =>
                                                                            controleur ! Sortie(Train1)      ← libère le tronçon immédiatement
                                                                            comportementHors(Train1, controleur) ← repart en hors → boucle infinie
                                                                       }
```

**Points clés :**
- `Train(...)` — appelle `apply()`, retourne juste un `Behavior` (le plan). L'acteur n'existe pas encore.
- `spawn(...)` — Akka crée vraiment l'acteur et exécute le `Behavior`.
- `Behaviors.setup` — s'exécute immédiatement à la création, donc `Demande` part avant la ligne suivante du test.
- Résumé : `Train(...)` fabrique le plan, `spawn` l'exécute.
