package com.heritier.millionnaire

import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

/**
 * Moteur de jeu pour le drag racing.
 * Gère la physique, le calcul des vitesses, les shifts, le nitro, etc.
 */
class DragRaceEngine(
    private val carStats: CarStats,
    private val raceState: RaceState,
    private val gameMode: GameMode = GameMode.GHOST_AI
) {
    companion object {
        private const val SHIFT_ZONE_MIN = 5000.0   // RPM minimum pour shift parfait
        private const val SHIFT_ZONE_MAX = 6500.0   // RPM maximum pour shift parfait
        private const val RPM_INCREASE_RATE = 2000.0 // RPM/seconde quand on accélère
        private const val RPM_DECREASE_RATE = 1500.0 // RPM/seconde quand on relâche
        private const val PERFECT_SHIFT_BONUS = 1.15  // Bonus de 15% pour shift parfait
        private const val BAD_SHIFT_PENALTY = 0.85    // Pénalité de 15% pour mauvais shift
        private const val DRAG_COEFFICIENT = 0.002    // Coefficient de résistance aérodynamique
    }

    private var isAccelerating = false
    private var opponentSpeed = 0.0
    private var opponentAverageSpeed = 0.0

    init {
        // Initialise la vitesse moyenne de l'adversaire selon le mode de jeu
        opponentAverageSpeed = when (gameMode) {
            GameMode.GHOST_AI -> {
                // IA a une vitesse moyenne avec un peu d'aléatoire
                val baseSpeed = 50.0 + Random.nextDouble(-5.0, 5.0)
                baseSpeed
            }
            GameMode.PVP -> 55.0 // TODO: récupérer depuis le serveur
            GameMode.WORLD_RECORD -> 60.0 // TODO: récupérer le record
        }
    }

    /**
     * Met à jour le jeu (appelé à chaque frame ~60 FPS)
     */
    fun update(deltaTimeSeconds: Double) {
        if (!raceState.isRunning) return

        // Met à jour le temps écoulé
        raceState.elapsedTimeSeconds += deltaTimeSeconds

        // Met à jour les RPM
        updateRpm(deltaTimeSeconds)

        // Calcule la vitesse du joueur
        val playerSpeed = calculatePlayerSpeed()

        // Met à jour la distance du joueur
        raceState.playerDistance += playerSpeed * deltaTimeSeconds

        // Met à jour l'adversaire
        updateOpponent(deltaTimeSeconds)

        // Vérifie la fin de course
        checkRaceEnd()
    }

    /**
     * Met à jour les RPM en fonction de l'accélération
     */
    private fun updateRpm(deltaTimeSeconds: Double) {
        // Gère le nitro
        if (carStats.isNitroActive) {
            val nitroElapsed = (System.currentTimeMillis() - carStats.nitroStartTime) / 1000.0
            if (nitroElapsed >= carStats.nitroDurationSeconds) {
                carStats.isNitroActive = false
            }
        }

        if (isAccelerating) {
            // Augmente les RPM
            carStats.rpm += RPM_INCREASE_RATE * deltaTimeSeconds
            carStats.rpm = min(carStats.rpm, carStats.maxRpm)
        } else {
            // Diminue les RPM vers le ralenti
            if (carStats.rpm > carStats.idleRpm) {
                carStats.rpm -= RPM_DECREASE_RATE * deltaTimeSeconds
                carStats.rpm = carStats.rpm.coerceAtLeast(carStats.idleRpm)
            }
        }

        // Enregistre le RPM max
        if (carStats.rpm > carStats.maxRpmReached) {
            carStats.maxRpmReached = carStats.rpm
        }
    }

    /**
     * Calcule la vitesse actuelle du joueur en m/s
     */
    private fun calculatePlayerSpeed(): Double {
        if (carStats.currentGear == 0) return 0.0

        // Vitesse de base dépend de la puissance moteur, RPM et rapport de vitesse
        val gearRatio = carStats.gearRatios[carStats.currentGear - 1]
        val rpmFactor = (carStats.rpm / carStats.maxRpm).coerceIn(0.0, 1.0)
        
        // Puissance effective (avec multiplicateur et nitro)
        var effectivePower = carStats.enginePower * carStats.enginePowerMultiplier
        if (carStats.isNitroActive) {
            effectivePower *= carStats.nitroPower
        }

        // Calcul de la vitesse (formule simplifiée)
        // vitesse (m/s) = puissance * RPM factor / gear ratio
        val baseSpeed = (effectivePower * rpmFactor) / (gearRatio * 10.0)

        // Applique la résistance aérodynamique
        val drag = DRAG_COEFFICIENT * baseSpeed.pow(2)
        val finalSpeed = (baseSpeed - drag).coerceAtLeast(0.0)

        // Enregistre la vitesse max en km/h
        val speedKmh = finalSpeed * 3.6
        if (speedKmh > carStats.maxSpeedReached) {
            carStats.maxSpeedReached = speedKmh
        }

        return finalSpeed
    }

    /**
     * Met à jour la position de l'adversaire
     */
    private fun updateOpponent(deltaTimeSeconds: Double) {
        // L'adversaire accélère progressivement jusqu'à sa vitesse moyenne
        if (opponentSpeed < opponentAverageSpeed) {
            opponentSpeed += 10.0 * deltaTimeSeconds
            opponentSpeed = min(opponentSpeed, opponentAverageSpeed)
        }

        // Ajoute un peu de variation aléatoire
        val variation = Random.nextDouble(-2.0, 2.0)
        val finalOpponentSpeed = opponentSpeed + variation

        raceState.opponentDistance += finalOpponentSpeed * deltaTimeSeconds
    }

    /**
     * Vérifie si la course est terminée
     */
    private fun checkRaceEnd() {
        if (raceState.playerDistance >= raceState.distanceTotal) {
            raceState.isRunning = false
            raceState.winner = "player"
        } else if (raceState.opponentDistance >= raceState.distanceTotal) {
            raceState.isRunning = false
            raceState.winner = "opponent"
        }
    }

    /**
     * Démarre l'accélération
     */
    fun startAccelerating() {
        isAccelerating = true
    }

    /**
     * Arrête l'accélération
     */
    fun stopAccelerating() {
        isAccelerating = false
    }

    /**
     * Effectue un changement de vitesse
     */
    fun shift(): ShiftResult {
        if (carStats.currentGear >= carStats.gearRatios.size) {
            // Déjà en dernière vitesse
            return ShiftResult(success = false, quality = "Dernière vitesse", bonus = 1.0)
        }

        // Vérifie si le shift est dans la zone verte
        val quality = when {
            carStats.rpm < SHIFT_ZONE_MIN - 1000 -> {
                raceState.lastShiftQuality = "Trop tôt"
                ShiftResult(success = true, quality = "Trop tôt", bonus = BAD_SHIFT_PENALTY)
            }
            carStats.rpm > SHIFT_ZONE_MAX + 500 -> {
                raceState.lastShiftQuality = "Trop tard"
                ShiftResult(success = true, quality = "Trop tard", bonus = BAD_SHIFT_PENALTY)
            }
            carStats.rpm in SHIFT_ZONE_MIN..SHIFT_ZONE_MAX -> {
                raceState.lastShiftQuality = "Parfait ✓"
                ShiftResult(success = true, quality = "Parfait ✓", bonus = PERFECT_SHIFT_BONUS)
            }
            else -> {
                raceState.lastShiftQuality = "Correct"
                ShiftResult(success = true, quality = "Correct", bonus = 1.0)
            }
        }

        // Change de vitesse
        carStats.currentGear++
        
        // Réduit les RPM lors du changement de vitesse
        carStats.rpm *= 0.7

        return quality
    }

    /**
     * Active le nitro
     */
    fun activateNitro(): Boolean {
        if (carStats.nitroCharges <= carStats.nitroChargesUsed || carStats.isNitroActive) {
            return false
        }

        carStats.isNitroActive = true
        carStats.nitroStartTime = System.currentTimeMillis()
        carStats.nitroChargesUsed++
        return true
    }

    /**
     * Démarre la course
     */
    fun startRace() {
        raceState.reset()
        carStats.reset()
        raceState.isRunning = true
        opponentSpeed = 0.0
    }

    /**
     * Récompense pour la victoire selon le mode de jeu
     */
    fun getReward(): Int {
        if (raceState.winner != "player") return 0
        
        return when (gameMode) {
            GameMode.GHOST_AI -> 50_000
            GameMode.PVP -> 500_000
            GameMode.WORLD_RECORD -> 1_000_000
        }
    }

    /**
     * Résultat d'un changement de vitesse
     */
    data class ShiftResult(
        val success: Boolean,
        val quality: String,
        val bonus: Double
    )
}
