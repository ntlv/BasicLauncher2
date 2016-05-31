package se.ntlv.basiclauncher.database

import android.content.SharedPreferences
import se.ntlv.basiclauncher.GridDimensions


class GlobalConfig(private val prefs: SharedPreferences) {

    private val GRID_DIMENS_ROW_COUNT = "GRID_DIMENS_ROW_COUNT"
    private val GRID_DIMENS_COL_COUNT = "GRID_DIMENS_COL_COUNT"

    private val ONE_TIME_INIT = "ONE_TIME_INIT"


    var pageDimens: GridDimensions
        set(newDimens: GridDimensions) {
            val editor = prefs.edit()
            editor.putInt(GRID_DIMENS_ROW_COUNT, newDimens.rowCount)
            editor.putInt(GRID_DIMENS_COL_COUNT, newDimens.columnCount)
            editor.apply()
        }
        get() {
            val rows = prefs.getInt(GRID_DIMENS_ROW_COUNT, 5)
            val cols = prefs.getInt(GRID_DIMENS_COL_COUNT, 4)
            return GridDimensions(rows, cols)
        }

    var shouldDoOneTimeInit : Boolean
        set(shouldDo: Boolean) = prefs.edit().putBoolean(ONE_TIME_INIT, shouldDo).apply()
        get() = prefs.getBoolean(ONE_TIME_INIT, true)
}