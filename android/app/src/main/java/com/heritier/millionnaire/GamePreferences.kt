package com.heritier.millionnaire

import android.content.Context
import android.content.SharedPreferences

/**
 * Helper pour gérer la persistance des données du jeu avec SharedPreferences
 */
class GamePreferences(context: Context) {

    companion object {
        private const val PREFS_NAME = "drag_race_prefs"
        
        // Clés pour l'argent et le numéro de course
        private const val KEY_MONEY = "money"
        private const val KEY_RACE_NUMBER = "race_number"
        
        // Clés pour les upgrades
        private const val KEY_ENGINE_LEVEL = "engine_level"
        private const val KEY_ENGINE_POWER = "engine_power"
        private const val KEY_TRANSMISSION_LEVEL = "transmission_level"
        
        // Clés pour les réglages
        private const val KEY_ENGINE_POWER_MULTIPLIER = "engine_power_multiplier"
        private const val KEY_NITRO_POWER = "nitro_power"
        private const val KEY_NITRO_DURATION = "nitro_duration"
        private const val KEY_NITRO_CHARGES = "nitro_charges"
        
        // Valeurs par défaut
        private const val DEFAULT_MONEY = 0
        private const val DEFAULT_RACE_NUMBER = 1
        private const val DEFAULT_ENGINE_LEVEL = 1
        private const val DEFAULT_ENGINE_POWER = 130.0
        private const val DEFAULT_TRANSMISSION_LEVEL = 1
        private const val DEFAULT_ENGINE_POWER_MULTIPLIER = 1.0
        private const val DEFAULT_NITRO_POWER = 1.4
        private const val DEFAULT_NITRO_DURATION = 1.5
        private const val DEFAULT_NITRO_CHARGES = 1
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ========== ARGENT ==========
    
    fun getMoney(): Int {
        return prefs.getInt(KEY_MONEY, DEFAULT_MONEY)
    }

    fun setMoney(amount: Int) {
        prefs.edit().putInt(KEY_MONEY, amount).apply()
    }

    fun addMoney(amount: Int) {
        val current = getMoney()
        setMoney(current + amount)
    }

    fun spendMoney(amount: Int): Boolean {
        val current = getMoney()
        if (current < amount) return false
        setMoney(current - amount)
        return true
    }

    // ========== NUMÉRO DE COURSE ==========
    
    fun getRaceNumber(): Int {
        return prefs.getInt(KEY_RACE_NUMBER, DEFAULT_RACE_NUMBER)
    }

    fun incrementRaceNumber() {
        val current = getRaceNumber()
        prefs.edit().putInt(KEY_RACE_NUMBER, current + 1).apply()
    }

    // ========== UPGRADES MOTEUR ==========
    
    fun getEngineLevel(): Int {
        return prefs.getInt(KEY_ENGINE_LEVEL, DEFAULT_ENGINE_LEVEL)
    }

    fun getEnginePower(): Double {
        return prefs.getFloat(KEY_ENGINE_POWER, DEFAULT_ENGINE_POWER.toFloat()).toDouble()
    }

    fun upgradeEngine(): Boolean {
        val currentLevel = getEngineLevel()
        if (currentLevel >= 20) return false // Niveau max
        
        val cost = 1_000_000
        if (!spendMoney(cost)) return false

        // Augmente la puissance de 20 HP par niveau
        val newLevel = currentLevel + 1
        val newPower = DEFAULT_ENGINE_POWER + (newLevel - 1) * 20.0

        prefs.edit()
            .putInt(KEY_ENGINE_LEVEL, newLevel)
            .putFloat(KEY_ENGINE_POWER, newPower.toFloat())
            .apply()

        return true
    }

    // ========== UPGRADES TRANSMISSION ==========
    
    fun getTransmissionLevel(): Int {
        return prefs.getInt(KEY_TRANSMISSION_LEVEL, DEFAULT_TRANSMISSION_LEVEL)
    }

    fun upgradeTransmission(): Boolean {
        val currentLevel = getTransmissionLevel()
        if (currentLevel >= 5) return false // Niveau max
        
        val cost = 1_000_000
        if (!spendMoney(cost)) return false

        val newLevel = currentLevel + 1
        prefs.edit()
            .putInt(KEY_TRANSMISSION_LEVEL, newLevel)
            .apply()

        return true
    }

    // ========== RÉGLAGES ==========
    
    fun getEnginePowerMultiplier(): Double {
        return prefs.getFloat(KEY_ENGINE_POWER_MULTIPLIER, DEFAULT_ENGINE_POWER_MULTIPLIER.toFloat()).toDouble()
    }

    fun setEnginePowerMultiplier(value: Double) {
        prefs.edit().putFloat(KEY_ENGINE_POWER_MULTIPLIER, value.toFloat()).apply()
    }

    fun getNitroPower(): Double {
        return prefs.getFloat(KEY_NITRO_POWER, DEFAULT_NITRO_POWER.toFloat()).toDouble()
    }

    fun setNitroPower(value: Double) {
        prefs.edit().putFloat(KEY_NITRO_POWER, value.toFloat()).apply()
    }

    fun getNitroDuration(): Double {
        return prefs.getFloat(KEY_NITRO_DURATION, DEFAULT_NITRO_DURATION.toFloat()).toDouble()
    }

    fun setNitroDuration(value: Double) {
        prefs.edit().putFloat(KEY_NITRO_DURATION, value.toFloat()).apply()
    }

    fun getNitroCharges(): Int {
        return prefs.getInt(KEY_NITRO_CHARGES, DEFAULT_NITRO_CHARGES)
    }

    fun setNitroCharges(value: Int) {
        prefs.edit().putInt(KEY_NITRO_CHARGES, value).apply()
    }

    /**
     * Réinitialise tous les réglages aux valeurs par défaut
     */
    fun resetSettings() {
        prefs.edit()
            .putFloat(KEY_ENGINE_POWER_MULTIPLIER, DEFAULT_ENGINE_POWER_MULTIPLIER.toFloat())
            .putFloat(KEY_NITRO_POWER, DEFAULT_NITRO_POWER.toFloat())
            .putFloat(KEY_NITRO_DURATION, DEFAULT_NITRO_DURATION.toFloat())
            .putInt(KEY_NITRO_CHARGES, DEFAULT_NITRO_CHARGES)
            .apply()
    }

    /**
     * Réinitialise TOUTES les données (argent, upgrades, réglages)
     * Utile pour debug ou remise à zéro complète
     */
    fun resetAll() {
        prefs.edit().clear().apply()
    }
}
