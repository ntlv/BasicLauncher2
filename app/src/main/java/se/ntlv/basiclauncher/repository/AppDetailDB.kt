package se.ntlv.basiclauncher.repository

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE
import android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.squareup.sqlbrite.BriteDatabase
import com.squareup.sqlbrite.QueryObservable
import com.squareup.sqlbrite.SqlBrite
import rx.schedulers.Schedulers


class AppDetailDB {

    companion object {
        val version = 2
        val tableName = "app_cache"
        val IS_DOCKED = "is_docked"
        val PACKAGE_NAME = "package_name"
        val LABEL = "label"
        val IS_IGNORED = "is_ignored"
        val ORDINAL = "ordinal"

        fun CREATE_TABLE(vararg field: String) = "create table $tableName(${field.joinToString ()}) "
    }

    private class Helper(ctx: Context, val appName: String) : SQLiteOpenHelper(ctx, tableName, null, version) {

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            Log.d("Helper", "UPGRADING DB")
            db?.execSQL("DROP TABLE IF EXISTS $tableName")
            onCreate(db)
        }

        override fun onCreate(db: SQLiteDatabase?) {
            Log.d("Helper", "CONSTRUCTING DB")

            db?.execSQL(CREATE_TABLE(
                    "$PACKAGE_NAME text primary key",
                    "$LABEL text default notext",
                    "$IS_DOCKED integer default 0",
                    "$IS_IGNORED integer default 0",
                    "$ORDINAL integer default 0"
            ))

            val seedIgnored = arrayOf(
                    appName
                    , "com.android.settings"
                    , "com.sonyericsson.organizer"
                    , "com.google.android.gms"
                    , "com.sonymobile.backgrounddefocus"
                    , "com.sonymobile.entrance"
                    , "com.giphy.messenger"
                    , "com.android.shell"
                    , "com.whirlscape.minuum"
                    , "com.google.android.googlequicksearchbox")
                    .map { AppDetail(it, it, false, true, 0).toCv() }

            val seedDocked = arrayOf(
                    "com.Slack",
                    "com.google.android.apps.inbox",
                    "com.facebook.orca",
                    "com.spotify.music.canary")
                    .mapIndexed { idx, name -> AppDetail(name, name, true, false, idx).toCv() }
            //TODO error handling lol
            seedIgnored.forEach { it: ContentValues -> db?.insert(tableName, null, it) }
            seedDocked.forEach { it: ContentValues -> db?.insert(tableName, null, it) }


        }
    }

    private val mDatabase: BriteDatabase


    constructor(ctx: Context, self: String) {
        Log.d("AppDetailDb", "CONSTRUCTING AN INSTANCE")
        val s = Helper(ctx, self)
        val briteDatabase = SqlBrite.create()
        mDatabase = briteDatabase.wrapDatabaseHelper(s, Schedulers.io())
        mDatabase.setLoggingEnabled(true)
    }

    fun getApps(isDocked: Boolean, isIgnored : Boolean): QueryObservable {
        val dockArg = isDocked.toInt().toString()
        val ignoredArg = isIgnored.toInt().toString()

        val select = "SELECT * FROM $tableName "
        val where = "WHERE $IS_DOCKED = ? AND $IS_IGNORED = ?"
        val orderBy = "ORDER BY $ORDINAL DESC, $PACKAGE_NAME ASC"

        return mDatabase.createQuery(tableName, "$select $where $orderBy", dockArg, ignoredArg)
    }

    fun insert(vararg apps: AppDetail, overwrite: Boolean = false): List<Long> {
        val conflictAlgorithm = when {
            overwrite -> CONFLICT_REPLACE
            else -> CONFLICT_IGNORE
        }
        val t = mDatabase.newTransaction()
        val results = apps.map { mDatabase.insert(tableName, it.toCv(), conflictAlgorithm) }
        t.markSuccessful()
        t.end()

        return results
    }

    fun delete(appPackageName: String): Int {
        return mDatabase.delete(tableName, "$PACKAGE_NAME = ?", appPackageName)
    }

    fun updatePackage(packageName: String, ordinal: Int? = null, ignore : Boolean? = null) {
        val cv = ContentValues(2)
        if (ordinal != null) cv.put("$ORDINAL", ordinal)
        if (ignore != null) cv.put("$IS_IGNORED", ignore.toInt())
        mDatabase.update(tableName, cv, "$PACKAGE_NAME = ?", packageName)
    }
}

fun Boolean.toInt(): Int {
    return if (this) 1 else 0
}
