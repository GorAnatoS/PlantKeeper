package com.goranatos.plantkeeper.ui

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.goranatos.plantkeeper.R
import com.goranatos.plantkeeper.ui.home.MyPlantsFragmentDirections
import com.goranatos.plantkeeper.ui.settings.IS_GO_TO_SETTINGS_AFTER_RESTART
import com.goranatos.plantkeeper.util.AnimationConstants
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI


class MainActivity : AppCompatActivity(), DIAware {

    override val di by closestDI()
    private lateinit var bottomNavView: BottomNavigationView
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        bottomNavView = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNavView.setupWithNavController(navController)


        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        toolbar.setupWithNavController(navController, appBarConfiguration)

        if (intent.getBooleanExtra(IS_GO_TO_SETTINGS_AFTER_RESTART, false)) {
            val navController = findNavController(R.id.nav_host_fragment)
            if (navController.currentDestination?.id != R.id.settingsFragment)
                navController.navigate(MyPlantsFragmentDirections.actionMyPlantsFragmentToSettingsFragment())
        }
    }

    private val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
        when (destination.id) {
            R.id.nav_my_plants, R.id.navigation_calendar -> {
                bottomNavView.visibility = View.VISIBLE
                bottomNavView.animate().alpha(1.0f).duration =
                    AnimationConstants.animationShortDuration
            }
            else -> {
                bottomNavView.animate().alpha(0.0f)
                    .setDuration(AnimationConstants.animationShortDuration).withEndAction {
                    bottomNavView.visibility = View.GONE
                }

            }
        }
    }


    override fun onResume() {
        super.onResume()
        navController.addOnDestinationChangedListener(listener)
    }

    override fun onPause() {
        navController.removeOnDestinationChangedListener(listener)
        super.onPause()
    }

    private fun runViewAnimation(view: View, animationIntId: Int) {
//        val context = view.context
//        val controller =
//            AnimationUtils.loadLayoutAnimation(context, animationIntId)

        val animation = ObjectAnimator.ofFloat(view, "translationX", 100f).apply {
            duration = 1000
            start()
        }

        // view.animation = animation
    }

}

// TODO: 5/10/2021  https://medium.com/swlh/curved-cut-out-bottom-navigation-with-animation-in-android-c630c867958c