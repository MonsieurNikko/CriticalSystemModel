// LancerDemo.scala : pont entre l'execution Scala et la demo HTML animee.
//
// Lance via : sbt "runMain m14.demo.LancerDemo"
//
// Etapes executees :
//   1. Regenere les 5 fichiers JSON de traces depuis le modele Petri verifie
//      (scenarios A, B, C, D, E).
//   2. Demarre un mini serveur HTTP local (port 8000 par defaut, ou premier port
//      libre dans 8000-8010) servant le dossier demo/.
//   3. Ouvre le navigateur par defaut sur http://localhost:<port>/index.html.
//   4. Affiche les commandes disponibles (Enter = arret) et attend.
//
// Aucun framework externe : utilise com.sun.net.httpserver (JDK builtin) et
// java.awt.Desktop. Compatible JDK 11+.

package m14.demo

import com.sun.net.httpserver.{HttpExchange, HttpHandler, HttpServer}

import java.awt.Desktop
import java.io.IOException
import java.net.{InetSocketAddress, URI}
import java.nio.file.{Files, Path, Paths}

object LancerDemo extends App {

  private val racineDemo: Path = Paths.get("demo").toAbsolutePath.normalize()
  private val portsCandidats: List[Int] = (8000 to 8010).toList

  // ---------------------------------------------------------------------------
  // Etape 1 : regeneration des traces JSON.
  // ---------------------------------------------------------------------------
  println("[1/3] Regeneration des traces JSON depuis le modele Petri verifie...")
  GenererTraces.main(Array.empty)
  println()

  // ---------------------------------------------------------------------------
  // Etape 2 : demarrage du serveur HTTP statique.
  // ---------------------------------------------------------------------------
  if (!Files.isDirectory(racineDemo)) {
    System.err.println(s"ERREUR : dossier ${racineDemo} introuvable. Lancer depuis la racine du projet.")
    sys.exit(1)
  }

  val (server, port) = demarrerServeur(racineDemo, portsCandidats) match {
    case Some(couple) => couple
    case None =>
      System.err.println(s"ERREUR : aucun port libre dans ${portsCandidats.head}-${portsCandidats.last}.")
      sys.exit(1)
  }
  val urlBase = s"http://localhost:$port/index.html"

  println(s"[2/3] Serveur HTTP demarre sur $urlBase")
  println(s"      (racine servie : $racineDemo)")
  println()

  // ---------------------------------------------------------------------------
  // Etape 3 : ouverture du navigateur.
  // ---------------------------------------------------------------------------
  println("[3/3] Ouverture du navigateur...")
  ouvrirNavigateur(urlBase)
  println()

  println("--- Demo en cours ---")
  println(s"  URL : $urlBase")
  println("  Selectionner un scenario (A, B, C, D, E) puis cliquer Lecture.")
  println("  5 scenarios disponibles :")
  println("    A - Cycle nominal complet (1 train, 7 etapes)")
  println("    B - Concurrence canton + quai (2 trains, 11 etapes)")
  println("    C - Cycle complet sequentiel des 2 trains (liveness, 13 etapes)")
  println("    D - Tentative PSD-Open invalide (CRITIQUE, 3 etapes)")
  println("    E - Tentative PSD-Departure portes ouvertes (CRITIQUE, 9 etapes)")
  println()
  println("  Appuyer sur Entree pour arreter le serveur et terminer.")

  // Bloque jusqu'a Entree (lecture ligne sur stdin).
  try {
    Console.in.readLine()
  } catch {
    case _: Throwable => ()
  }

  println("Arret du serveur...")
  server.stop(0)
  println("Termine.")

  // ===========================================================================
  // Helpers
  // ===========================================================================

  private def demarrerServeur(racine: Path, ports: List[Int]): Option[(HttpServer, Int)] = {
    ports.iterator.flatMap { port =>
      try {
        val srv = HttpServer.create(new InetSocketAddress("localhost", port), 0)
        srv.createContext("/", new ServeurFichiersStatiques(racine))
        srv.setExecutor(null) // executor par defaut
        srv.start()
        Some((srv, port))
      } catch {
        case _: IOException => None
      }
    }.toSeq.headOption
  }

  private def ouvrirNavigateur(url: String): Unit = {
    if (Desktop.isDesktopSupported && Desktop.getDesktop.isSupported(Desktop.Action.BROWSE)) {
      try {
        Desktop.getDesktop.browse(new URI(url))
      } catch {
        case e: Throwable =>
          System.err.println(s"  Ouverture automatique impossible : ${e.getMessage}")
          System.err.println(s"  -> Ouvrir manuellement : $url")
      }
    } else {
      println(s"  (Desktop non supporte sur cette plateforme. Ouvrir manuellement : $url)")
    }
  }
}

// Handler HTTP minimal qui sert les fichiers statiques d'un dossier racine.
// Refuse toute tentative de path traversal (../).
private final class ServeurFichiersStatiques(racine: Path) extends HttpHandler {

  private val typesContenu: Map[String, String] = Map(
    ".html" -> "text/html; charset=utf-8",
    ".json" -> "application/json; charset=utf-8",
    ".js"   -> "application/javascript; charset=utf-8",
    ".css"  -> "text/css; charset=utf-8",
    ".png"  -> "image/png",
    ".svg"  -> "image/svg+xml",
    ".ico"  -> "image/x-icon"
  )

  override def handle(echange: HttpExchange): Unit = {
    try {
      val cheminBrut = echange.getRequestURI.getPath match {
        case "/" | "" => "/index.html"
        case autre    => autre
      }
      val cible = racine.resolve(cheminBrut.stripPrefix("/")).normalize()

      // Securite : empeche la sortie hors du dossier racine.
      if (!cible.startsWith(racine)) {
        envoyer(echange, 403, "Forbidden".getBytes("UTF-8"), "text/plain")
        return
      }

      if (!Files.exists(cible) || Files.isDirectory(cible)) {
        envoyer(echange, 404, s"404 Not Found: $cheminBrut".getBytes("UTF-8"), "text/plain")
        return
      }

      val octets = Files.readAllBytes(cible)
      val ext = {
        val nom = cible.getFileName.toString
        val idx = nom.lastIndexOf('.')
        if (idx >= 0) nom.substring(idx).toLowerCase else ""
      }
      val mime = typesContenu.getOrElse(ext, "application/octet-stream")
      // Cache disable pour que la regeneration de traces soit immediatement visible.
      echange.getResponseHeaders.add("Cache-Control", "no-store")
      envoyer(echange, 200, octets, mime)
    } catch {
      case e: Throwable =>
        envoyer(echange, 500, s"500 Internal Server Error: ${e.getMessage}".getBytes("UTF-8"), "text/plain")
    } finally {
      echange.close()
    }
  }

  private def envoyer(echange: HttpExchange, code: Int, octets: Array[Byte], mime: String): Unit = {
    echange.getResponseHeaders.add("Content-Type", mime)
    echange.sendResponseHeaders(code, octets.length.toLong)
    val flux = echange.getResponseBody
    try flux.write(octets) finally flux.close()
  }
}
