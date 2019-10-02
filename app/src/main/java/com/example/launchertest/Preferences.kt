package com.example.launchertest

import android.content.Context
import androidx.core.content.edit

object Preferences {
    const val FIRST_LOAD = "0"
    const val MAIN_SCREEN_ICONS_COUNT = "1"
    const val ALLAPPS_COLUMN_COUNT= "2"
    const val ALLAPPS_ROW_COUNT = "3"

    // here default values is declared
    fun loadDefaultPreferences(context: Context) {
        getPrefs(context).edit {
            putBoolean(FIRST_LOAD, false)
            putInt(MAIN_SCREEN_ICONS_COUNT, 5)
            putInt(ALLAPPS_COLUMN_COUNT, 5)
            putInt(ALLAPPS_ROW_COUNT, 7)
        }
    }
}