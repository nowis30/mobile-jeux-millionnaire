package com.heritier.millionnaire

/**
 * Enum pour les différents modes de jeu
 */
enum class GameMode {
    GHOST_AI,        // Course contre IA fantôme (+50 000 $)
    PVP,             // Course PvP en ligne (+500 000 $)
    WORLD_RECORD     // Tentative de record mondial (+1 000 000 $)
}

/**
 * Data class pour les statistiques de la voiture
 */
data class CarStats(
    var enginePower: Double = 130.0,           // Puissance moteur en HP
    var engineLevel: Int = 1,                   // Niveau moteur (1-20)
    var gearRatios: List<Double> = listOf(     // Rapports de vitesse
        3.5, 2.5, 1.8, 1.3, 1.0
    ),
    var transmissionLevel: Int = 1,             // Niveau transmission (1-5)
    var currentGear: Int = 0,                   // Vitesse actuelle (0 = neutre)
    var rpm: Double = 0.0,                      // RPM actuel
    var maxRpm: Double = 7000.0,                // RPM maximum
    var idleRpm: Double = 800.0,                // RPM au ralenti
    var nitroPower: Double = 1.4,               // Multiplicateur de puissance nitro
    var nitroDurationSeconds: Double = 1.5,     // Durée du nitro en secondes
    var nitroCharges: Int = 1,                  // Nombre de charges nitro disponibles
    var nitroChargesUsed: Int = 0,              // Charges utilisées dans cette course
    var isNitroActive: Boolean = false,         // Nitro actif ?
    var nitroStartTime: Long = 0,               // Moment du démarrage nitro
    
    // Paramètres de réglages (modifiables dans le garage)
    var enginePowerMultiplier: Double = 1.0,    // Multiplicateur de force moteur
    var maxSpeedReached: Double = 0.0,          // Vitesse max atteinte (pour stats)
    var maxRpmReached: Double = 0.0             // RPM max atteint (pour stats)
) {
    fun reset() {
        currentGear = 0
        rpm = idleRpm
        nitroChargesUsed = 0
        isNitroActive = false
        nitroStartTime = 0
        maxSpeedReached = 0.0
        maxRpmReached = 0.0
    }
}

/**
 * Data class pour l'état de la course
 */
data class RaceState(
    val distanceTotal: Double = 400.0,          // Distance totale en mètres (1/4 mile = ~402m)
    var playerDistance: Double = 0.0,           // Distance parcourue par le joueur
    var opponentDistance: Double = 0.0,         // Distance parcourue par l'adversaire
    var elapsedTimeSeconds: Double = 0.0,       // Temps écoulé en secondes
    var isRunning: Boolean = false,             // Course en cours ?
    var winner: String? = null,                 // "player", "opponent", ou null
    var lastShiftQuality: String = "—",         // "Parfait", "Trop tôt", "Trop tard", "—"
    var raceNumber: Int = 1                     // Numéro de course actuel
) {
    fun reset() {
        playerDistance = 0.0
        opponentDistance = 0.0
        elapsedTimeSeconds = 0.0
        isRunning = false
        winner = null
        lastShiftQuality = "—"
    }
}
