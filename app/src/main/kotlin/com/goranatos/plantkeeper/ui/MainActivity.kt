package com.goranatos.plantkeeper.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.goranatos.plantkeeper.R
import com.goranatos.plantkeeper.ui.home.MyPlantsFragmentDirections
import com.goranatos.plantkeeper.ui.settings.IS_GO_TO_SETTINGS_AFTER_RESTART
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI

class MainActivity : AppCompatActivity(), DIAware {

    override val di by closestDI()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navController = findNavController(R.id.nav_host_fragment)

        navController.let {
            val appBarConfiguration = AppBarConfiguration(navController.graph)
            val toolbar = findViewById<Toolbar>(R.id.toolbar)
            setSupportActionBar(toolbar)
            toolbar.setupWithNavController(it, appBarConfiguration)
        }

        if (intent.getBooleanExtra(IS_GO_TO_SETTINGS_AFTER_RESTART, false)) {
            val navController = findNavController(R.id.nav_host_fragment)
            if (navController.currentDestination?.id != R.id.settingsFragment)
                navController.navigate(MyPlantsFragmentDirections.actionMyPlantsFragmentToSettingsFragment())
        }
    }
}