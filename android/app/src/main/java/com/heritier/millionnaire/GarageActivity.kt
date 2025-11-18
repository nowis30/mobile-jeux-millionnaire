package com.heritier.millionnaire

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

/**
 * Activity du garage pour gérer les améliorations et réglages
 */
class GarageActivity : AppCompatActivity() {

    companion object {
        /**
         * Méthode statique pour démarrer l'activité depuis n'importe où
         */
        fun start(context: Context) {
            val intent = Intent(context, GarageActivity::class.java)
            context.startActivity(intent)
        }
    }

    private lateinit var tvGarageMoney: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var btnBack: Button
    private lateinit var preferences: GamePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_garage)

        preferences = GamePreferences(this)

        initViews()
        setupViewPager()
        updateMoney()
    }

    private fun initViews() {
        tvGarageMoney = findViewById(R.id.tvGarageMoney)
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupViewPager() {
        val adapter = GaragePagerAdapter(this)
        viewPager.adapter = adapter

        // Connecte le TabLayout au ViewPager
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Améliorations"
                1 -> "Réglages"
                else -> ""
            }
        }.attach()
    }

    fun updateMoney() {
        tvGarageMoney.text = formatMoney(preferences.getMoney()) + " $"
    }

    private fun formatMoney(amount: Int): String {
        return amount.toString().reversed()
            .chunked(3)
            .joinToString(" ")
            .reversed()
    }

    /**
     * Adapter pour le ViewPager avec les deux onglets
     */
    private class GaragePagerAdapter(activity: GarageActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> UpgradesFragment()
                1 -> SettingsFragment()
                else -> UpgradesFragment()
            }
        }
    }
}
