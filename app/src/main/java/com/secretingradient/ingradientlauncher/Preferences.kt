package com.secretingradient.ingradientlauncher

import android.content.Context
import androidx.core.content.edit

object Preferences {
    const val FIRST_LOAD = "0"
    const val MAIN_STAGE_ICONS_COUNT = "1"
    const val MAIN_STAGE_WIDTH_CELL = "7"
    const val MAIN_STAGE_HEIGHT_CELL = "8"
    const val USER_STAGE_COLUMN_COUNT = "5"
    const val USER_STAGE_ROW_COUNT = "4"
    const val USER_STAGE_PAGE_COUNT = "6"
    const val ALLAPPS_STAGE_COLUMN_COUNT = "2"
    const val ALLAPPS_STAGE_ROW_COUNT = "3"

    // here default values is declared
    fun loadDefaultPreferences(context: Context) {
        getPrefs(context).edit {
            putBoolean(FIRST_LOAD, false)
            putInt(MAIN_STAGE_ICONS_COUNT, 5)
            putInt(ALLAPPS_STAGE_ROW_COUNT, 8)
            putInt(ALLAPPS_STAGE_COLUMN_COUNT, 4)
            putInt(USER_STAGE_ROW_COUNT, 5)
            putInt(USER_STAGE_COLUMN_COUNT, 7)
            putInt(USER_STAGE_PAGE_COUNT, 3)
            putInt(MAIN_STAGE_WIDTH_CELL, 120)
            putInt(MAIN_STAGE_HEIGHT_CELL, 120)
        }
    }
}