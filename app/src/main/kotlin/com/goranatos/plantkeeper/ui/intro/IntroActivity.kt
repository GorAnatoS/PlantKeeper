package com.goranatos.plantkeeper.ui.intro

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro2
import com.github.appintro.AppIntroFragment
import com.goranatos.plantkeeper.R

/**
 * Created by qsufff on 4/11/2021.
 */

class IntroActivity : AppIntro2() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isWizardMode = true

        addSlide(
            AppIntroFragment.newInstance(
                getString(R.string.slide_1_title),
                getString(R.string.slide_1_desc),
                R.drawable.slide_1,
                backgroundDrawable = R.drawable.back_slide1,
            )
        )

        addSlide(
            AppIntroFragment.newInstance(
                getString(R.string.slide_2_title),
                getString(R.string.slide_2_desc),
                R.drawable.slide_2,
                backgroundDrawable = R.drawable.back_slide2,
            )
        )

        addSlide(
            AppIntroFragment.newInstance(
                getString(R.string.slide_3_title),
                getString(R.string.slide_3_desc),
                R.drawable.slide_3,
                backgroundDrawable = R.drawable.back_slide3,
            )
        )
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        finish()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        finish()
    }
}