package com.goranatos.plantskeeper.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.goranatos.plantskeeper.R
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


    }
}

// TODO: 11/22/2020 что надо
//название цветка +
//примечание
//картинка +
//необходимый полив раз в +
//необходима подкормка раз в
//необходима опрыскивание
//пересадка
//обрезанj,htие
//поворот
//сделать полив по сезонам +

//help

//about_application option
// TODO: 1/17/2021 Добавить в будущих версиях
//const val SPRAY_NEED_COLUMN = "spraying_need"
//const val REPLANT_NEED_COLUMN = "replanting_need"
//const val CUT_NEED_COLUMN = "cutting_need"
//const val TURN_NEED_COLUMN = "turning_need"

// TODO: 1/18/2021 TEST OPEN APP PAGE