/*
 * Copyright (C) 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.goranatos.plantskeeper.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.goranatos.plantskeeper.R
import com.goranatos.plantskeeper.ui.MainActivity
import com.goranatos.plantskeeper.ui.settings.LANGUAGE_PREFS.LANGUAGE_ENGLISH
import com.goranatos.plantskeeper.ui.settings.LANGUAGE_PREFS.LANGUAGE_ENGLISH_COUNTRY
import com.goranatos.plantskeeper.ui.settings.LANGUAGE_PREFS.LANGUAGE_RUSSIAN
import com.goranatos.plantskeeper.ui.settings.LANGUAGE_PREFS.LANGUAGE_RUSSIAN_COUNTRY
import com.yariksoffice.lingver.Lingver


/**
 * A subclass of PreferenceFragmentCompat to supply preferences in a
 * Fragment for the SettingsActivity to display.
 */
class SettingsFragment : PreferenceFragmentCompat() {
    private val DIALOG_FRAGMENT_TAG = "TimePickerDialog"

    private lateinit var startedAppLanguage: String
    private var finishedAppLanguage: String = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            callback
        )

        val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun onBackPressed() {
        findNavController().navigateUp()
        restartAppIfNeeded()
    }

    private fun restartAppIfNeeded() {
        if (finishedAppLanguage.isNotEmpty() && finishedAppLanguage != startedAppLanguage) {

            when (finishedAppLanguage) {
                "ru" -> setNewLocale(
                    requireContext(),
                    LANGUAGE_RUSSIAN,
                    LANGUAGE_RUSSIAN_COUNTRY
                )
                "en" -> setNewLocale(
                    requireContext(),
                    LANGUAGE_ENGLISH,
                    LANGUAGE_ENGLISH_COUNTRY
                )
            }

            val i = Intent(context, MainActivity::class.java)
            startActivity(i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    private fun setNewLocale(context: Context, language: String, country: String) {
        Lingver.getInstance().setLocale(context, language, country)
    }

    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?
    ) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        PreferenceManager.getDefaultSharedPreferences(requireContext()).getString(
            getString(R.string.pref_option_choose_language), getString(
                R.string.en
            )
        )
            ?.also { startedAppLanguage = it }

        val chooseLanguagePreference =
            findPreference<ListPreference>(getString(R.string.pref_option_choose_language))

        if (chooseLanguagePreference?.entry == null) {
            chooseLanguagePreference?.title = getString(R.string.current_lang)
        } else {
            chooseLanguagePreference.title = chooseLanguagePreference.entry
        }

        chooseLanguagePreference?.setOnPreferenceChangeListener { preference, newValue ->
            if (preference is ListPreference) {
                finishedAppLanguage = newValue.toString()

                val index = preference.findIndexOfValue(newValue.toString())
                val entry = preference.entries[index]

                preference.title = entry
            }
            true
        }
    }

    override fun onDisplayPreferenceDialog(preference: Preference?) {
        if (preference is TimePickerPreference) {
            val timePickerDialog = TimePickerPreferenceDialog.newInstance(preference.key)
            timePickerDialog.setTargetFragment(this, 0)
            timePickerDialog.show(parentFragmentManager, DIALOG_FRAGMENT_TAG)
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }
}

object LANGUAGE_PREFS {
    const val LANGUAGE_ENGLISH = "en"
    const val LANGUAGE_ENGLISH_COUNTRY = "US"
    const val LANGUAGE_RUSSIAN = "ru"
    const val LANGUAGE_RUSSIAN_COUNTRY = "RU"
}