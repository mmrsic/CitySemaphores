package com.citysemaphores.domain.model

import kotlin.math.max

/**
 * Repräsentiert ein Fahrzeug in der Simulation.
 *
 * Ein Fahrzeug folgt einer vorberechneten Route und kann sich in verschiedenen Zuständen befinden.
 * Die Punktzahl setzt sich zusammen aus:
 * - Basisptunkte: +1 pro passierter Kreuzung
 * - Bonuspunkte: Länge der Route (totalDistance) minus Wartezeit
 *
 * @property id Eindeutige ID des Fahrzeugs
 * @property position Aktuelle Position (kontinuierlich, nicht diskret)
 * @property route Route, der das Fahrzeug folgt
 * @property speed Geschwindigkeit in Einheiten pro Sekunde
 * @property state Aktueller Zustand des Fahrzeugs
 * @property crossingsPassed Anzahl der bereits passierten Kreuzungen (Basispunkte)
 * @property waitTime Akkumulierte Wartezeit in Sekunden
 * @property isInCollision Ob das Fahrzeug in eine Kollision verwickelt wurde
 */
data class Vehicle(
    val id: String,
    val position: Position,
    val route: Route,
    val speed: Float,
    val state: VehicleState = VehicleState.Moving,
    val crossingsPassed: Int = 0,
    val waitTime: Float = 0f,
    val isInCollision: Boolean = false
) {
    init {
        require(id.isNotBlank()) { "Vehicle ID cannot be blank" }
        require(speed > 0f) { "Vehicle speed must be positive, got $speed" }
        require(crossingsPassed >= 0) { "crossingsPassed cannot be negative" }
        require(waitTime >= 0f) { "waitTime cannot be negative" }
    }

    /**
     * Bewegt das Fahrzeug basierend auf der vergangenen Zeit.
     *
     * - **Moving**: Bewegt sich in Richtung der nächsten Kreuzung
     * - **Waiting**: Position bleibt gleich, waitTime wird erhöht
     * - **Arrived/Crashed**: Keine Bewegung
     *
     * @param deltaTime Vergangene Zeit in Sekunden
     * @return Aktualisiertes Fahrzeug
     */
    fun move(deltaTime: Float): Vehicle {
        return when (state) {
            VehicleState.Moving -> {
                val target = route.next?.let { Position(it.position.x.toDouble(), it.position.y.toDouble()) }
                    ?: Position(route.current.position.x.toDouble(), route.current.position.y.toDouble())

                val direction = (target - position).normalized()
                val distance = speed * deltaTime
                val newPosition = position + (direction * distance.toDouble())

                copy(position = newPosition)
            }
            VehicleState.Waiting -> {
                // Position bleibt gleich, aber Wartezeit erhöht sich
                copy(waitTime = waitTime + deltaTime)
            }
            VehicleState.Arrived, VehicleState.Crashed -> {
                // Keine Bewegung
                this
            }
        }
    }

    /**
     * Erhöht den Zähler für passierte Kreuzungen um 1.
     * Wird aufgerufen, wenn das Fahrzeug eine Kreuzung verlässt.
     */
    fun passCrossing(): Vehicle =
        copy(crossingsPassed = crossingsPassed + 1)

    /**
     * Berechnet die Gesamtpunktzahl für dieses Fahrzeug.
     *
     * Formel: score = crossingsPassed + max(0, totalDistance - waitTime)
     *
     * - crossingsPassed: Basispunkte (+1 pro Kreuzung)
     * - totalDistance: Bonuspunkte für die Streckenlänge
     * - waitTime: Abzug für Wartezeit (kann Bonus auf 0 reduzieren)
     *
     * Der Spieler erhält mindestens die Basispunkte, auch wenn die Wartezeit
     * die Distanz übersteigt.
     *
     * @return Die Gesamtpunktzahl
     */
    fun calculateScore(): Int {
        val baseScore = crossingsPassed
        val bonus = max(0, route.totalDistance - waitTime.toInt())
        return baseScore + bonus
    }

    /**
     * Setzt das Fahrzeug in den Wartezustand
     */
    fun startWaiting(): Vehicle =
        copy(state = VehicleState.Waiting)

    /**
     * Setzt das Fahrzeug zurück in den Bewegungszustand
     */
    fun continueMoving(): Vehicle =
        if (state == VehicleState.Waiting) copy(state = VehicleState.Moving) else this

    /**
     * Markiert das Fahrzeug als angekommen
     */
    fun markAsArrived(): Vehicle =
        copy(state = VehicleState.Arrived)

    /**
     * Markiert das Fahrzeug als verunglückt
     */
    fun markAsCrashed(): Vehicle =
        copy(
            state = VehicleState.Crashed,
            isInCollision = true
        )

    override fun toString(): String =
        "Vehicle($id, state=$state, at=${route.currentIndex}/${route.path.size-1}, crossings=$crossingsPassed, wait=${waitTime.toInt()}s)"
}
