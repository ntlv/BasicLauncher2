package se.ntlv.basiclauncher.repository

import android.content.ContentValues
import android.database.Cursor
import se.ntlv.basiclauncher.repository.AppDetailDB.Companion.IS_DOCKED
import se.ntlv.basiclauncher.repository.AppDetailDB.Companion.IS_IGNORED
import se.ntlv.basiclauncher.repository.AppDetailDB.Companion.LABEL
import se.ntlv.basiclauncher.repository.AppDetailDB.Companion.ORDINAL
import se.ntlv.basiclauncher.repository.AppDetailDB.Companion.PACKAGE_NAME

data class AppDetail(
        val label: String,
        val packageName: String,
        val isDock: Boolean,
        val isIgnored: Boolean,
        val ordinal : Int
) {
    constructor(c : Cursor) : this(
            c.getString(c.getColumnIndex(LABEL)),
            c.getString(c.getColumnIndex(PACKAGE_NAME)),
            c.getInt(c.getColumnIndex(IS_DOCKED)) == 1,
            c.getInt(c.getColumnIndex(IS_IGNORED)) == 1,
            c.getInt(c.getColumnIndex(ORDINAL))
            )

    fun toCv(): ContentValues {
        val cv = ContentValues(5)
        cv.put(PACKAGE_NAME, packageName)
        cv.put(LABEL, label)
        cv.put(IS_DOCKED, if (isDock) 1 else 0)
        cv.put(IS_IGNORED, if (isIgnored) 1 else 0)
        cv.put(AppDetailDB.ORDINAL, ordinal)
        return cv
    }
}
