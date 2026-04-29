// PetriNet.scala : structures de donnees et operations pour un reseau de Petri ordinaire.
// Modele etendu PSD (cf petri-troncon.md) : canton + quai + portes palieres = 12 places, 12 transitions.

package m14.petri

object PetriNet {

  // Une place est identifiee par son nom (coherent avec petri-troncon.md section 2).
  type Place = String

  // Un marquage associe a chaque place son nombre de jetons.
  type Marking = Map[Place, Int]

  // Une transition a un nom, des arcs entrants (pre) et des arcs sortants (post).
  // Pre : places consommees (1 jeton chacune). Post : places produites (1 jeton chacune).
  // Pour emuler un read-arc on inscrit la place dans pre ET dans post (consommation+reproduction).
  final case class Transition(nom: String, pre: Set[Place], post: Set[Place])

  // Le reseau complet : ses places, ses transitions, son marquage initial.
  final case class Net(places: Set[Place], transitions: List[Transition], marquageInitial: Marking)

  // ===========================================================================
  // Les 12 places (cf petri-troncon.md section 2)
  // ===========================================================================

  // Ressources globales (4)
  val CantonLibre    = "Canton_libre"
  val QuaiLibre      = "Quai_libre"
  val PortesFermees  = "Portes_fermees"
  val PortesOuvertes = "Portes_ouvertes"

  // Etats du Train 1 (4)
  val T1Hors      = "T1_hors"
  val T1Attente   = "T1_attente"
  val T1SurCanton = "T1_sur_canton"
  val T1AQuai     = "T1_a_quai"

  // Etats du Train 2 (4)
  val T2Hors      = "T2_hors"
  val T2Attente   = "T2_attente"
  val T2SurCanton = "T2_sur_canton"
  val T2AQuai     = "T2_a_quai"

  val toutesLesPlaces: Set[Place] = Set(
    CantonLibre, QuaiLibre, PortesFermees, PortesOuvertes,
    T1Hors, T1Attente, T1SurCanton, T1AQuai,
    T2Hors, T2Attente, T2SurCanton, T2AQuai
  )

  // ===========================================================================
  // Les 12 transitions (cf petri-troncon.md section 3)
  // ===========================================================================

  // Cycle Train 1 (4 transitions)
  val t1Demande         = Transition("T1_demande",         Set(T1Hors),                 Set(T1Attente))
  val t1EntreeCanton    = Transition("T1_entree_canton",   Set(T1Attente, CantonLibre), Set(T1SurCanton))
  val t1ArriveeQuai     = Transition("T1_arrivee_quai",    Set(T1SurCanton, QuaiLibre), Set(T1AQuai, CantonLibre))
  // Read-arc emule sur PortesFermees : la place est consommee ET reproduite.
  val t1DepartQuai      = Transition("T1_depart_quai",     Set(T1AQuai, PortesFermees), Set(T1Hors, QuaiLibre, PortesFermees))

  // Cycle Train 2 (4 transitions, symetriques)
  val t2Demande         = Transition("T2_demande",         Set(T2Hors),                 Set(T2Attente))
  val t2EntreeCanton    = Transition("T2_entree_canton",   Set(T2Attente, CantonLibre), Set(T2SurCanton))
  val t2ArriveeQuai     = Transition("T2_arrivee_quai",    Set(T2SurCanton, QuaiLibre), Set(T2AQuai, CantonLibre))
  val t2DepartQuai      = Transition("T2_depart_quai",     Set(T2AQuai, PortesFermees), Set(T2Hors, QuaiLibre, PortesFermees))

  // Gestion des portes palieres (4 transitions)
  // Read-arc emule sur Ti_a_quai : la presence du train a quai est lue+reproduite.
  val ouvertureT1       = Transition("Ouverture_portes_T1", Set(PortesFermees, T1AQuai),  Set(PortesOuvertes, T1AQuai))
  val ouvertureT2       = Transition("Ouverture_portes_T2", Set(PortesFermees, T2AQuai),  Set(PortesOuvertes, T2AQuai))
  val fermetureT1       = Transition("Fermeture_portes_T1", Set(PortesOuvertes, T1AQuai), Set(PortesFermees, T1AQuai))
  val fermetureT2       = Transition("Fermeture_portes_T2", Set(PortesOuvertes, T2AQuai), Set(PortesFermees, T2AQuai))

  val toutesLesTransitions: List[Transition] = List(
    t1Demande, t1EntreeCanton, t1ArriveeQuai, t1DepartQuai,
    t2Demande, t2EntreeCanton, t2ArriveeQuai, t2DepartQuai,
    ouvertureT1, ouvertureT2, fermetureT1, fermetureT2
  )

  // ===========================================================================
  // Marquage initial M0 (5 jetons en circulation, cf petri-troncon.md section 2)
  // ===========================================================================
  val marquageInitial: Marking = Map(
    CantonLibre -> 1, QuaiLibre -> 1, PortesFermees -> 1, PortesOuvertes -> 0,
    T1Hors -> 1, T1Attente -> 0, T1SurCanton -> 0, T1AQuai -> 0,
    T2Hors -> 1, T2Attente -> 0, T2SurCanton -> 0, T2AQuai -> 0
  )

  // Le reseau complet du sous-systeme canton + quai + portes.
  val reseauTroncon: Net = Net(toutesLesPlaces, toutesLesTransitions, marquageInitial)

  // ===========================================================================
  // Operations
  // ===========================================================================

  // Une transition est tirable si chaque place de son pre a au moins 1 jeton.
  def estTirable(transition: Transition, marquage: Marking): Boolean =
    transition.pre.forall(place => marquage.getOrElse(place, 0) >= 1)

  // Tire une transition depuis un marquage et retourne le nouveau marquage.
  // Retourne None si la transition n'est pas tirable.
  def tirer(transition: Transition, marquage: Marking): Option[Marking] = {
    if (!estTirable(transition, marquage)) {
      None
    } else {
      val apresConsommation = transition.pre.foldLeft(marquage) { (m, place) =>
        m.updated(place, m.getOrElse(place, 0) - 1)
      }
      val apresProduction = transition.post.foldLeft(apresConsommation) { (m, place) =>
        m.updated(place, m.getOrElse(place, 0) + 1)
      }
      Some(apresProduction)
    }
  }

  // Liste les transitions tirables depuis un marquage donne.
  def transitionsTirables(net: Net, marquage: Marking): List[Transition] =
    net.transitions.filter(t => estTirable(t, marquage))
}
