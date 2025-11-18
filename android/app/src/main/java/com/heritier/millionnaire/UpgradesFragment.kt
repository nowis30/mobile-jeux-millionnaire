package com.heritier.millionnaire

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

/**
 * Fragment pour les améliorations (moteur, transmission)
 */
class UpgradesFragment : Fragment() {

    private lateinit var preferences: GamePreferences
    
    private lateinit var tvEngineLevel: TextView
    private lateinit var tvEnginePower: TextView
    private lateinit var btnUpgradeEngine: Button
    
    private lateinit var tvTransmissionLevel: TextView
    private lateinit var tvTransmissionGears: TextView
    private lateinit var btnUpgradeTransmission: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_upgrades, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferences = GamePreferences(requireContext())

        initViews(view)
        setupListeners()
        updateUI()
    }

    private fun initViews(view: View) {
        tvEngineLevel = view.findViewById(R.id.tvEngineLevel)
        tvEnginePower = view.findViewById(R.id.tvEnginePower)
        btnUpgradeEngine = view.findViewById(R.id.btnUpgradeEngine)
        
        tvTransmissionLevel = view.findViewById(R.id.tvTransmissionLevel)
        tvTransmissionGears = view.findViewById(R.id.tvTransmissionGears)
        btnUpgradeTransmission = view.findViewById(R.id.btnUpgradeTransmission)
    }

    private fun setupListeners() {
        btnUpgradeEngine.setOnClickListener {
            val success = preferences.upgradeEngine()
            if (success) {
                Toast.makeText(requireContext(), "Moteur amélioré!", Toast.LENGTH_SHORT).show()
                updateUI()
                updateGarageMoney()
            } else {
                val level = preferences.getEngineLevel()
                if (level >= 20) {
                    Toast.makeText(requireContext(), "Niveau maximum atteint!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Pas assez d'argent!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnUpgradeTransmission.setOnClickListener {
            val success = preferences.upgradeTransmission()
            if (success) {
                Toast.makeText(requireContext(), "Transmission améliorée!", Toast.LENGTH_SHORT).show()
                updateUI()
                updateGarageMoney()
            } else {
                val level = preferences.getTransmissionLevel()
                if (level >= 5) {
                    Toast.makeText(requireContext(), "Niveau maximum atteint!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Pas assez d'argent!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateUI() {
        // Moteur
        val engineLevel = preferences.getEngineLevel()
        val enginePower = preferences.getEnginePower()
        tvEngineLevel.text = "Niveau $engineLevel/20"
        tvEnginePower.text = "${enginePower.toInt()} HP ${getEngineName(engineLevel)}"
        
        if (engineLevel >= 20) {
            btnUpgradeEngine.text = "NIVEAU MAX"
            btnUpgradeEngine.isEnabled = false
        } else {
            btnUpgradeEngine.text = "Améliorer - 1 000 000 $"
            btnUpgradeEngine.isEnabled = true
        }

        // Transmission
        val transmissionLevel = preferences.getTransmissionLevel()
        tvTransmissionLevel.text = "Niveau $transmissionLevel/5"
        tvTransmissionGears.text = "${4 + transmissionLevel} vitesses ${getTransmissionName(transmissionLevel)}"
        
        if (transmissionLevel >= 5) {
            btnUpgradeTransmission.text = "NIVEAU MAX"
            btnUpgradeTransmission.isEnabled = false
        } else {
            btnUpgradeTransmission.text = "Améliorer - 1 000 000 $"
            btnUpgradeTransmission.isEnabled = true
        }
    }

    private fun getEngineName(level: Int): String {
        return when (level) {
            1 -> "(Corolla)"
            in 2..5 -> "(Civic)"
            in 6..10 -> "(Mustang)"
            in 11..15 -> "(Supra)"
            else -> "(F1)"
        }
    }

    private fun getTransmissionName(level: Int): String {
        return when (level) {
            1 -> "Corolla (fixe)"
            2 -> "Civic Sport"
            3 -> "Mustang GT"
            4 -> "Supra Racing"
            5 -> "F1 Sequential"
            else -> ""
        }
    }

    private fun updateGarageMoney() {
        (activity as? GarageActivity)?.updateMoney()
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }
}
