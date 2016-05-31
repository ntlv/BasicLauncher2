package se.ntlv.basiclauncher.database

import android.content.ContentValues
import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import se.ntlv.basiclauncher.database.AppDetailDB.Companion.COLUMN
import se.ntlv.basiclauncher.database.AppDetailDB.Companion.IS_DOCKED
import se.ntlv.basiclauncher.database.AppDetailDB.Companion.IS_IGNORED
import se.ntlv.basiclauncher.database.AppDetailDB.Companion.LABEL
import se.ntlv.basiclauncher.database.AppDetailDB.Companion.PACKAGE_NAME
import se.ntlv.basiclauncher.database.AppDetailDB.Companion.PAGE
import se.ntlv.basiclauncher.database.AppDetailDB.Companion.ROW

data class AppDetail(
        val label: String,
        val packageName: String,
        val isDock: Boolean,
        val isIgnored: Boolean,
        val page: Int,
        val row: Int,
        val column: Int) : Parcelable {

    companion object {
        @JvmField @Suppress("unused")
        val CREATOR = createParcel({ it: Parcel -> AppDetail(it) })
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(label)
        dest.writeString(packageName)
        dest.writeInt(isDock.toInt())
        dest.writeInt(isIgnored.toInt())
        dest.writeInt(page)
        dest.writeInt(row)
        dest.writeInt(column)
    }

    override fun describeContents() = 0

    constructor(c: Cursor) : this(
            c.getString(c.getColumnIndex(LABEL)),
            c.getString(c.getColumnIndex(PACKAGE_NAME)),
            c.getInt(c.getColumnIndex(IS_DOCKED)) == 1,
            c.getInt(c.getColumnIndex(IS_IGNORED)) == 1,
            c.getInt(c.getColumnIndex(PAGE)),
            c.getInt(c.getColumnIndex(ROW)),
            c.getInt(c.getColumnIndex(COLUMN))
    )

    constructor(p: Parcel) : this(
            p.readString(),
            p.readString(),
            p.readInt().toBoolean(),
            p.readInt().toBoolean(),
            p.readInt(),
            p.readInt(),
            p.readInt()
    )

    fun toCv(): ContentValues {
        val cv = ContentValues(7)
        cv.put(PACKAGE_NAME, packageName)
        cv.put(LABEL, label)
        cv.put(IS_DOCKED, if (isDock) 1 else 0)
        cv.put(IS_IGNORED, if (isIgnored) 1 else 0)
        cv.put(PAGE, page)
        cv.put(ROW, row)
        cv.put(COLUMN, column)
        return cv
    }

    fun isPlaceholder(): Boolean {
        return label == "PLACEHOLDER"
    }
}

inline fun <reified T : Parcelable> createParcel(
        crossinline createFromParcel: (Parcel) -> T?): Parcelable.Creator<T> =
        object : Parcelable.Creator<T> {
            override fun createFromParcel(source: Parcel): T? = createFromParcel(source)
            override fun newArray(size: Int): Array<out T?> = arrayOfNulls(size)
        }