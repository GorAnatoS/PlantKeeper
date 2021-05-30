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
package com.goranatos.plantkeeper.ui.settings

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.*
import com.goranatos.plantkeeper.R
import com.goranatos.plantkeeper.ui.MainActivity
import com.goranatos.plantkeeper.ui.settings.LanguagePrefs.LANGUAGE_ENGLISH
import com.goranatos.plantkeeper.ui.settings.LanguagePrefs.LANGUAGE_ENGLISH_COUNTRY
import com.goranatos.plantkeeper.ui.settings.LanguagePrefs.LANGUAGE_RUSSIAN
import com.goranatos.plantkeeper.ui.settings.LanguagePrefs.LANGUAGE_RUSSIAN_COUNTRY
import com.goranatos.plantkeeper.ui.settings.LanguagePrefs.LANGUAGE_SPANISH
import com.goranatos.plantkeeper.ui.settings.LanguagePrefs.LANGUAGE_SPANISH_COUNTRY
import com.yariksoffice.lingver.Lingver


/**
 * A subclass of PreferenceFragmentCompat to supply preferences in a
 * Fragment for the SettingsActivity to display.
 */

const val IS_GO_TO_SETTINGS_AFTER_RESTART = "is_go_to_settings_after_restart"
private const val DIALOG_FRAGMENT_TAG = "TimePickerDialog"

class SettingsFragment : PreferenceFragmentCompat() {
    private var beginAppLanguage: String = ""
    private var endAppLanguage: String = ""

    private var beginAppTheme = false
    private var endAppTheme = false

    private fun checkIsLanguageChangeNeeded() {
        if (endAppLanguage.isNotEmpty() && endAppLanguage != beginAppLanguage) {

            when (endAppLanguage) {
                getString(R.string.ru) -> setNewLocale(
                    requireContext(),
                    LANGUAGE_RUSSIAN,
                    LANGUAGE_RUSSIAN_COUNTRY
                )
                getString(R.string.en) -> setNewLocale(
                    requireContext(),
                    LANGUAGE_ENGLISH,
                    LANGUAGE_ENGLISH_COUNTRY
                )
                getString(R.string.es) -> setNewLocale(
                    requireContext(),
                    LANGUAGE_SPANISH,
                    LANGUAGE_SPANISH_COUNTRY
                )
            }

            val i = Intent(context, MainActivity::class.java)
            i.putExtra(IS_GO_TO_SETTINGS_AFTER_RESTART, true)
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
                R.string.empty_string
            )
        )
            ?.also { beginAppLanguage = it }

        val chooseLanguagePreference =
            findPreference<ListPreference>(getString(R.string.pref_option_choose_language))

        if (chooseLanguagePreference?.entry == null) {
            chooseLanguagePreference?.title = getString(R.string.current_lang)
        } else {
            chooseLanguagePreference.title = chooseLanguagePreference.entry
        }

        chooseLanguagePreference?.setOnPreferenceChangeListener { preference, newValue ->
            if (preference is ListPreference) {
                endAppLanguage = newValue.toString()

                val index = preference.findIndexOfValue(newValue.toString())
                val entry = preference.entries[index]

                preference.title = entry

                checkIsLanguageChangeNeeded()
            }
            true
        }


        PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean(
            getString(R.string.pref_option_is_dark_theme_enabled), false
        ).also { beginAppTheme = it }

        val chooseThemePreference =
            findPreference<SwitchPreferenceCompat>(getString(R.string.pref_option_is_dark_theme_enabled))

        when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                beginAppTheme = true
                chooseThemePreference?.isChecked = beginAppTheme
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                beginAppTheme = false
                chooseThemePreference?.isChecked = beginAppTheme
            }
        }

        chooseThemePreference?.setOnPreferenceChangeListener { preference, newValue ->

            endAppTheme = newValue as Boolean

            changeApplicationThemeIfNeeded()

            true
        }
    }


    private fun changeApplicationThemeIfNeeded() {
        if (endAppTheme != beginAppTheme) {
            when (endAppTheme) {
                false -> AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO
                )
                true -> AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES
                )
            }
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

object LanguagePrefs {
    const val LANGUAGE_ENGLISH = "en"
    const val LANGUAGE_ENGLISH_COUNTRY = "US"
    const val LANGUAGE_RUSSIAN = "ru"
    const val LANGUAGE_RUSSIAN_COUNTRY = "RU"
    const val LANGUAGE_SPANISH = "es"
    const val LANGUAGE_SPANISH_COUNTRY = "SP"
}