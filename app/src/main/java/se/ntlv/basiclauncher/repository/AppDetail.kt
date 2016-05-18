package se.ntlv.basiclauncher.repository

import android.content.ContentValues

data class AppDetail(
        val label: String,
        val packageName: String,
        val isDock: Boolean,
        val isIgnored: Boolean
) {
    fun toCv(): ContentValues {
        val cv = ContentValues(4)
        cv.put(AppDetailDB.PACKAGE_NAME, packageName)
        cv.put(AppDetailDB.LABEL, label)
        cv.put(AppDetailDB.IS_DOCKED, if (isDock) 1 else 0)
        cv.put(AppDetailDB.IS_IGNORED, if (isIgnored) 1 else 0)
        return cv
    }
}
