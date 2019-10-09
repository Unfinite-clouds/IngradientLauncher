package com.secretingradient.ingradientlauncher

import android.content.Context
import androidx.core.content.edit

object Preferences {
    const val FIRST_LOAD = "0"
    const val MAIN_SCREEN_ICONS_COUNT = "1"
    const val ALLAPPS_COLUMN_COUNT = "2"
    const val ALLAPPS_ROW_COUNT = "3"
    const val CUSTOM_GRID_ROW_COUNT = "4"
    const val CUSTOM_GRID_COLUMN_COUNT = "5"
    const val CUSTOM_GRID_PAGE_COUNT = "6"
    const val MAIN_SCREEN_WIDTH_CELL = "7"
    const val MAIN_SCREEN_HEIGHT_CELL = "8"

    // here default values is declared
    fun loadDefaultPreferences(context: Context) {
        getPrefs(context).edit {
            putBoolean(FIRST_LOAD, false)
            putInt(MAIN_SCREEN_ICONS_COUNT, 5)
            putInt(ALLAPPS_ROW_COUNT, 8)
            putInt(ALLAPPS_COLUMN_COUNT, 4)
            putInt(CUSTOM_GRID_ROW_COUNT, 5)
            putInt(CUSTOM_GRID_COLUMN_COUNT, 7)
            putInt(CUSTOM_GRID_PAGE_COUNT, 3)
            putInt(MAIN_SCREEN_WIDTH_CELL, 120)
            putInt(MAIN_SCREEN_HEIGHT_CELL, 120)
        }
    }
}