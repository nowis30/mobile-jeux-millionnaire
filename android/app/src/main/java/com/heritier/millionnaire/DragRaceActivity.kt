package com.heritier.millionnaire

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import android.content.Context
import android.content.Intent
import kotlin.math.roundToInt

/**
 * Activity principale du jeu de drag racing.
 * Gère l'interface, la boucle de jeu et les interactions utilisateur.
 */
class DragRaceActivity : AppCompatActivity() {

    companion object {
        private const val GAME_TICK_MS = 16L  // ~60 FPS
        
        /**
         * Méthode statique pour démarrer l'activité depuis n'importe où
         */
        fun start(context: Context) {
            val intent = Intent(context, DragRaceActivity::class.java)
            context.startActivity(intent)
        }
    }

    // Views UI
    private lateinit var tvRaceNumber: TextView
    private lateinit var tvMoney: TextView
    private lateinit var tvTime: TextView
    private lateinit var tvLastShift: TextView
    private lateinit var tvMaxSpeed: TextView
    private lateinit var tvMaxRpm: TextView
    private lateinit var ivPlayerCar: ImageView
    private lateinit var ivOpponentCar: ImageView
    private lateinit var pbRpm: ProgressBar
    private lateinit var tvCurrentRpm: TextView
    private lateinit var tvCurrentGear: TextView
    private lateinit var btnAccelerate: Button
    private lateinit var btnShift: Button
    private lateinit var btnNitro: Button
    private lateinit var btnStartRace: Button
    private lateinit var flRaceTrack: FrameLayout

    // Game logic
    private lateinit var carStats: CarStats
    private lateinit var raceState: RaceState
    private lateinit var engine: DragRaceEngine
    private lateinit var preferences: GamePreferences

    // Game loop
    private val handler = Handler(Looper.getMainLooper())
    private var lastUpdateTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drag_race)

        // Initialise les préférences
        preferences = GamePreferences(this)

        // Initialise le jeu
        initGame()

        // Initialise l'UI
        initViews()
        setupListeners()
        updateUI()
    }

    /**
     * Initialise le jeu avec les stats sauvegardées
     */
    private fun initGame() {
        // Charge les stats depuis les préférences
        carStats = CarStats(
            enginePower = preferences.getEnginePower(),
            engineLevel = preferences.getEngineLevel(),
            transmissionLevel = preferences.getTransmissionLevel(),
            enginePowerMultiplier = preferences.getEnginePowerMultiplier(),
            nitroPower = preferences.getNitroPower(),
            nitroDurationSeconds = preferences.getNitroDuration(),
            nitroCharges = preferences.getNitroCharges()
        )

        raceState = RaceState(
            raceNumber = preferences.getRaceNumber()
        )

        engine = DragRaceEngine(carStats, raceState, GameMode.GHOST_AI)
    }

    /**
     * Initialise toutes les vues
     */
    private fun initViews() {
        // HUD
        tvRaceNumber = findViewById(R.id.tvRaceNumber)
        tvMoney = findViewById(R.id.tvMoney)
        tvTime = findViewById(R.id.tvTime)
        tvLastShift = findViewById(R.id.tvLastShift)
        tvMaxSpeed = findViewById(R.id.tvMaxSpeed)
        tvMaxRpm = findViewById(R.id.tvMaxRpm)

        // Piste et voitures
        flRaceTrack = findViewById(R.id.flRaceTrack)
        ivPlayerCar = findViewById(R.id.ivPlayerCar)
        ivOpponentCar = findViewById(R.id.ivOpponentCar)

        // Cadran RPM
        pbRpm = findViewById(R.id.pbRpm)
        tvCurrentRpm = findViewById(R.id.tvCurrentRpm)
        tvCurrentGear = findViewById(R.id.tvCurrentGear)

        // Boutons
        btnAccelerate = findViewById(R.id.btnAccelerate)
        btnShift = findViewById(R.id.btnShift)
        btnNitro = findViewById(R.id.btnNitro)
        btnStartRace = findViewById(R.id.btnStartRace)

        // Configure le RPM max de la progress bar
        pbRpm.max = carStats.maxRpm.toInt()
    }

    /**
     * Configure les listeners des boutons
     */
    private fun setupListeners() {
        // Bouton accélérateur (maintien)
        btnAccelerate.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (raceState.isRunning) {
                        engine.startAccelerating()
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    engine.stopAccelerating()
                    true
                }
                else -> false
            }
        }

        // Bouton shift
        btnShift.setOnClickListener {
            if (raceState.isRunning) {
                val result = engine.shift()
                Toast.makeText(this, result.quality, Toast.LENGTH_SHORT).show()
            }
        }

        // Bouton nitro
        btnNitro.setOnClickListener {
            if (raceState.isRunning) {
                val activated = engine.activateNitro()
                if (activated) {
                    Toast.makeText(this, "NITRO ACTIVÉ!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Nitro non disponible", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Bouton démarrer la course
        btnStartRace.setOnClickListener {
            startRace()
        }

        // Bouton pour ouvrir le garage (on l'ajoute au header)
        tvMoney.setOnClickListener {
            GarageActivity.start(this)
        }
    }

    /**
     * Démarre une nouvelle course
     */
    private fun startRace() {
        engine.startRace()
        lastUpdateTime = System.currentTimeMillis()
        
        // Lance la boucle de jeu
        gameLoop()

        // Met à jour l'UI
        btnStartRace.isEnabled = false
        btnStartRace.alpha = 0.5f
    }

    /**
     * Boucle de jeu principale
     */
    private fun gameLoop() {
        if (!raceState.isRunning) {
            onRaceFinished()
            return
        }

        val currentTime = System.currentTimeMillis()
        val deltaTime = (currentTime - lastUpdateTime) / 1000.0
        lastUpdateTime = currentTime

        // Met à jour la logique du jeu
        engine.update(deltaTime)

        // Met à jour l'interface
        updateUI()
        updateCarPositions()

        // Programme le prochain tick
        handler.postDelayed({ gameLoop() }, GAME_TICK_MS)
    }

    /**
     * Met à jour toute l'interface
     */
    private fun updateUI() {
        // HUD
        tvRaceNumber.text = "Course ${raceState.raceNumber}"
        tvMoney.text = "Argent: ${formatMoney(preferences.getMoney())} $"
        tvTime.text = "Temps: ${"%.2f".format(raceState.elapsedTimeSeconds)} s"
        tvLastShift.text = "Dernier shift: ${raceState.lastShiftQuality}"
        tvMaxSpeed.text = "Vitesse max: ${carStats.maxSpeedReached.roundToInt()} km/h"
        tvMaxRpm.text = "RPM max: ${carStats.maxRpmReached.roundToInt()}"

        // Cadran RPM
        pbRpm.progress = carStats.rpm.toInt()
        tvCurrentRpm.text = "${carStats.rpm.roundToInt()} RPM"
        
        val gearText = when (carStats.currentGear) {
            0 -> "N"
            else -> carStats.currentGear.toString()
        }
        tvCurrentGear.text = "Vitesse: $gearText"

        // Colore la barre RPM selon la zone
        pbRpm.progressTint = when {
            carStats.rpm in 5000.0..6500.0 -> getColor(android.R.color.holo_green_light)
            carStats.rpm > 6500.0 -> getColor(android.R.color.holo_red_light)
            else -> getColor(android.R.color.holo_orange_light)
        }

        // Bouton nitro
        val nitroRemaining = carStats.nitroCharges - carStats.nitroChargesUsed
        btnNitro.text = "NOS ($nitroRemaining)"
        btnNitro.isEnabled = nitroRemaining > 0 && !carStats.isNitroActive
        btnNitro.alpha = if (btnNitro.isEnabled) 1.0f else 0.5f

        // Indicateur nitro actif
        if (carStats.isNitroActive) {
            btnNitro.setBackgroundColor(getColor(android.R.color.holo_purple))
        }
    }

    /**
     * Met à jour la position des voitures sur la piste
     */
    private fun updateCarPositions() {
        val trackWidth = flRaceTrack.width.toFloat()
        val startMargin = 32f
        val endMargin = 32f
        val availableWidth = trackWidth - startMargin - endMargin

        // Calcule la position du joueur (0.0 à 1.0)
        val playerProgress = (raceState.playerDistance / raceState.distanceTotal).coerceIn(0.0, 1.0)
        val playerX = startMargin + (availableWidth * playerProgress).toFloat()
        ivPlayerCar.x = playerX

        // Calcule la position de l'adversaire
        val opponentProgress = (raceState.opponentDistance / raceState.distanceTotal).coerceIn(0.0, 1.0)
        val opponentX = startMargin + (availableWidth * opponentProgress).toFloat()
        ivOpponentCar.x = opponentX
    }

    /**
     * Appelé quand la course se termine
     */
    private fun onRaceFinished() {
        // Réactive le bouton démarrer
        btnStartRace.isEnabled = true
        btnStartRace.alpha = 1.0f

        // Calcule la récompense
        val reward = engine.getReward()
        val isVictory = raceState.winner == "player"

        if (isVictory) {
            preferences.addMoney(reward)
            preferences.incrementRaceNumber()
            raceState.raceNumber = preferences.getRaceNumber()
        }

        // Affiche le résultat
        showResultDialog(isVictory, reward)
    }

    /**
     * Affiche le dialogue de résultat
     */
    private fun showResultDialog(victory: Boolean, reward: Int) {
        val title = if (victory) "🏆 VICTOIRE!" else "😢 DÉFAITE"
        val message = buildString {
            if (victory) {
                appendLine("Félicitations! Vous avez gagné!")
                appendLine()
                appendLine("Récompense: ${formatMoney(reward)} $")
            } else {
                appendLine("L'adversaire a gagné cette fois.")
                appendLine("Réessayez!")
            }
            appendLine()
            appendLine("Temps: ${"%.2f".format(raceState.elapsedTimeSeconds)} s")
            appendLine("Vitesse max: ${carStats.maxSpeedReached.roundToInt()} km/h")
            appendLine("RPM max: ${carStats.maxRpmReached.roundToInt()}")
        }

        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Nouvelle course") { dialog, _ ->
                dialog.dismiss()
                updateUI()
            }
            .setNeutralButton("Garage") { dialog, _ ->
                dialog.dismiss()
                GarageActivity.start(this)
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Formate l'argent avec des espaces pour les milliers
     */
    private fun formatMoney(amount: Int): String {
        return amount.toString().reversed()
            .chunked(3)
            .joinToString(" ")
            .reversed()
    }

    override fun onResume() {
        super.onResume()
        // Recharge les stats au retour du garage
        initGame()
        updateUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Arrête la boucle de jeu
        handler.removeCallbacksAndMessages(null)
    }
}
