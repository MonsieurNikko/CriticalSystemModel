package m14

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object StationControl {

  sealed trait Command

  final case class PassengerFlowUpdate(zone: Zone, occupancy: Int) extends Command
  final case class ReportIncident(id: String, zone: Zone, severity: Severity, description: String) extends Command
  final case class ResolveIncident(id: String) extends Command
  final case class GetSnapshot(replyTo: ActorRef[Snapshot]) extends Command

  sealed trait Zone {
    def name: String
  }

  case object PlatformNorth extends Zone {
    override val name: String = "platform-north"
  }

  case object PlatformSouth extends Zone {
    override val name: String = "platform-south"
  }

  case object TransferCorridor extends Zone {
    override val name: String = "transfer-corridor"
  }

  sealed trait Severity
  case object Low extends Severity
  case object High extends Severity
  case object Critical extends Severity

  sealed trait Mode
  case object Normal extends Mode
  case object Safety extends Mode

  final case class Incident(id: String, zone: Zone, severity: Severity, description: String)
  final case class ZoneState(occupancy: Int, threshold: Int, gateOpen: Boolean)

  final case class Snapshot(
      mode: Mode,
      zones: Map[Zone, ZoneState],
      openIncidents: Map[String, Incident],
      alerts: Vector[String]
  )

  final case class Config(thresholds: Map[Zone, Int], safetyBuffer: Int)

  private final case class State(
      mode: Mode,
      zones: Map[Zone, ZoneState],
      openIncidents: Map[String, Incident],
      alerts: Vector[String]
  )

  val defaultConfig: Config = Config(
    thresholds = Map(
      PlatformNorth -> 900,
      PlatformSouth -> 900,
      TransferCorridor -> 1200
    ),
    safetyBuffer = 250
  )

  def apply(config: Config = defaultConfig): Behavior[Command] = Behaviors.setup { _ =>
    val initialZones = config.thresholds.map { case (zone, threshold) =>
      zone -> ZoneState(occupancy = 0, threshold = threshold, gateOpen = true)
    }

    active(State(Normal, initialZones, Map.empty, Vector.empty), config)
  }

  private def active(state: State, config: Config): Behavior[Command] = Behaviors.receive { (_, message) =>
    message match {
      case PassengerFlowUpdate(zone, occupancy) =>
        val currentZone = state.zones(zone)
        val overThreshold = occupancy > currentZone.threshold
        val severeOverThreshold = occupancy > (currentZone.threshold + config.safetyBuffer)

        val updatedZone = currentZone.copy(
          occupancy = occupancy,
          gateOpen = if (state.mode == Safety) false else !overThreshold
        )

        val zonesAfterUpdate = state.zones.updated(zone, updatedZone)
        val alertsAfterUpdate =
          if (overThreshold)
            state.alerts :+ s"OVER_THRESHOLD:${zone.name}:$occupancy>${currentZone.threshold}"
          else state.alerts

        if (severeOverThreshold) {
          val lockedZones = closeAllGates(zonesAfterUpdate)
          val safetyAlert =
            s"SAFETY_MODE_TRIGGERED_BY_DENSITY:${zone.name}:$occupancy>${currentZone.threshold + config.safetyBuffer}"

          active(
            state.copy(mode = Safety, zones = lockedZones, alerts = alertsAfterUpdate :+ safetyAlert),
            config
          )
        } else {
          val nextMode = if (hasCriticalIncident(state.openIncidents)) Safety else state.mode
          val nextZones = if (nextMode == Safety) closeAllGates(zonesAfterUpdate) else zonesAfterUpdate

          active(state.copy(mode = nextMode, zones = nextZones, alerts = alertsAfterUpdate), config)
        }

      case ReportIncident(id, zone, severity, description) =>
        val incident = Incident(id, zone, severity, description)
        val incidentsAfterUpdate = state.openIncidents.updated(id, incident)

        val incidentAlert = s"INCIDENT:${id}:${zone.name}:$severity"
        val alertsAfterUpdate = state.alerts :+ incidentAlert

        val nextMode = if (severity == Critical) Safety else state.mode
        val nextZones = if (nextMode == Safety) closeAllGates(state.zones) else state.zones

        active(
          state.copy(mode = nextMode, zones = nextZones, openIncidents = incidentsAfterUpdate, alerts = alertsAfterUpdate),
          config
        )

      case ResolveIncident(id) =>
        val incidentsAfterUpdate = state.openIncidents - id

        val shouldStayInSafety = hasCriticalIncident(incidentsAfterUpdate) || hasOvercrowdedZone(state.zones)
        val nextMode = if (shouldStayInSafety) Safety else Normal
        val nextZones =
          if (nextMode == Safety) closeAllGates(state.zones)
          else reopenSafeGates(state.zones)

        val resolutionAlert = s"INCIDENT_RESOLVED:$id"

        active(
          state.copy(mode = nextMode, zones = nextZones, openIncidents = incidentsAfterUpdate, alerts = state.alerts :+ resolutionAlert),
          config
        )

      case GetSnapshot(replyTo) =>
        replyTo ! Snapshot(state.mode, state.zones, state.openIncidents, state.alerts)
        Behaviors.same
    }
  }

  private def closeAllGates(zones: Map[Zone, ZoneState]): Map[Zone, ZoneState] =
    zones.map { case (zone, zoneState) => zone -> zoneState.copy(gateOpen = false) }

  private def reopenSafeGates(zones: Map[Zone, ZoneState]): Map[Zone, ZoneState] =
    zones.map { case (zone, zoneState) =>
      val isSafe = zoneState.occupancy <= zoneState.threshold
      zone -> zoneState.copy(gateOpen = isSafe)
    }

  private def hasCriticalIncident(incidents: Map[String, Incident]): Boolean =
    incidents.values.exists(_.severity == Critical)

  private def hasOvercrowdedZone(zones: Map[Zone, ZoneState]): Boolean =
    zones.values.exists(zoneState => zoneState.occupancy > zoneState.threshold)
}
