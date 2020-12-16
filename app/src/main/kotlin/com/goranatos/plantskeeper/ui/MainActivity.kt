package com.goranatos.plantskeeper.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.goranatos.plantskeeper.R
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI

class MainActivity : AppCompatActivity(), DIAware {

    override val di by closestDI()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
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