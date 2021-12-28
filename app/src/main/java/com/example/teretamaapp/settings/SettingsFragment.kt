package com.example.teretamaapp.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.teretamaapp.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val themePreference = findPreference<ListPreference>("theme")
        themePreference?.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()

        // Figure out current state
        val settingToSet = themeResolveValue()

        themePreference?.value = settingToSet

        // Handle theme change
        themePreference?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            if (newValue is String) {
                return@OnPreferenceChangeListener themeSet(newValue)
            } else {
                return@OnPreferenceChangeListener false
            }
        }

        val titleLanguagePreference = findPreference<ListPreference>("title_language")
        titleLanguagePreference?.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
    }
}

/**
 * Resolve which setting value should be currently set.
 * @return Setting value.
 */
fun themeResolveValue(): String {
    return when (AppCompatDelegate.getDefaultNightMode()) {
        AppCompatDelegate.MODE_NIGHT_NO -> "light"
        AppCompatDelegate.MODE_NIGHT_YES -> "dark"
        else -> "default"
    }
}

/**
 * Set app theme based on current setting value.
 * @param value Current setting value.
 */
fun themeSet(value: String?): Boolean {
    when(value) {
        "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    return true
}