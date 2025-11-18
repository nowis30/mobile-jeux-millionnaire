package com.heritier.millionnaire

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

/**
 * Fragment pour les réglages (force moteur, nitro, etc.)
 */
class SettingsFragment : Fragment() {

    private lateinit var preferences: GamePreferences
    
    private lateinit var sbEnginePower: SeekBar
    private lateinit var tvEnginePowerValue: TextView
    
    private lateinit var sbNitroPower: SeekBar
    private lateinit var tvNitroPowerValue: TextView
    
    private lateinit var sbNitroDuration: SeekBar
    private lateinit var tvNitroDurationValue: TextView
    
    private lateinit var sbNitroCharges: SeekBar
    private lateinit var tvNitroChargesValue: TextView
    
    private lateinit var btnReset: Button
    private lateinit var btnApply: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferences = GamePreferences(requireContext())

        initViews(view)
        setupListeners()
        loadSettings()
    }

    private fun initViews(view: View) {
        sbEnginePower = view.findViewById(R.id.sbEnginePower)
        tvEnginePowerValue = view.findViewById(R.id.tvEnginePowerValue)
        
        sbNitroPower = view.findViewById(R.id.sbNitroPower)
        tvNitroPowerValue = view.findViewById(R.id.tvNitroPowerValue)
        
        sbNitroDuration = view.findViewById(R.id.sbNitroDuration)
        tvNitroDurationValue = view.findViewById(R.id.tvNitroDurationValue)
        
        sbNitroCharges = view.findViewById(R.id.sbNitroCharges)
        tvNitroChargesValue = view.findViewById(R.id.tvNitroChargesValue)
        
        btnReset = view.findViewById(R.id.btnReset)
        btnApply = view.findViewById(R.id.btnApply)
    }

    private fun setupListeners() {
        // Force moteur (0.50× à 2.00×)
        sbEnginePower.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = progress / 100.0
                tvEnginePowerValue.text = String.format("%.2f×", value)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Nitro power (1.00× à 2.00×)
        sbNitroPower.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = progress / 100.0
                tvNitroPowerValue.text = String.format("%.2f×", value)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Nitro duration (0.5s à 5.0s)
        sbNitroDuration.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = progress / 10.0
                tvNitroDurationValue.text = String.format("%.1f s", value)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Nitro charges (1 à 10)
        sbNitroCharges.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvNitroChargesValue.text = progress.toString()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Bouton réinitialiser
        btnReset.setOnClickListener {
            preferences.resetSettings()
            loadSettings()
            Toast.makeText(requireContext(), "Réglages réinitialisés", Toast.LENGTH_SHORT).show()
        }

        // Bouton appliquer
        btnApply.setOnClickListener {
            saveSettings()
            Toast.makeText(requireContext(), "Réglages appliqués", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadSettings() {
        // Charge les valeurs depuis les préférences
        val enginePower = preferences.getEnginePowerMultiplier()
        sbEnginePower.progress = (enginePower * 100).toInt()
        tvEnginePowerValue.text = String.format("%.2f×", enginePower)

        val nitroPower = preferences.getNitroPower()
        sbNitroPower.progress = (nitroPower * 100).toInt()
        tvNitroPowerValue.text = String.format("%.2f×", nitroPower)

        val nitroDuration = preferences.getNitroDuration()
        sbNitroDuration.progress = (nitroDuration * 10).toInt()
        tvNitroDurationValue.text = String.format("%.1f s", nitroDuration)

        val nitroCharges = preferences.getNitroCharges()
        sbNitroCharges.progress = nitroCharges
        tvNitroChargesValue.text = nitroCharges.toString()
    }

    private fun saveSettings() {
        // Sauvegarde les valeurs dans les préférences
        val enginePower = sbEnginePower.progress / 100.0
        preferences.setEnginePowerMultiplier(enginePower)

        val nitroPower = sbNitroPower.progress / 100.0
        preferences.setNitroPower(nitroPower)

        val nitroDuration = sbNitroDuration.progress / 10.0
        preferences.setNitroDuration(nitroDuration)

        val nitroCharges = sbNitroCharges.progress
        preferences.setNitroCharges(nitroCharges)
    }
}
