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
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton


@Singleton
class AppDetailDB {

    companion object {
        val version = 1
        val tableName = "app_cache"
        val IS_DOCKED = "is_docked"
        val PACKAGE_NAME = "package_name"
        val LABEL = "label"
        val IS_IGNORED = "is_ignored"

        fun CREATE_TABLE(vararg field: String) = "create table $tableName(${field.joinToString ()}) "
    }

    class Helper(ctx: Context, val appName: String) : SQLiteOpenHelper(ctx, tableName, null, version) {
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
                    "$IS_IGNORED integer default 0"


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
                    .map { AppDetail(it, it, false, true).toCv() }

            val seedDocked = arrayOf(
                    "com.google.android.apps.inbox",
                    "com.spotify.music.canary",
                    "com.facebook.orca",
                    "com.Slack")
                    .map { AppDetail(it, it, true, false).toCv() }
            //TODO error handling lol
            seedIgnored.forEach { it: ContentValues -> db?.insert(tableName, null, it) }
            seedDocked.forEach { it: ContentValues -> db?.insert(tableName, null, it) }


        }
    }

    private val brite: BriteDatabase

    @Inject
    constructor(ctx: Context, @Named("appName") self: String) {
        Log.d("AppDetailDb", "CONSTRUCTING AN INSTANCE")
        val s = Helper(ctx, self)
        val briteBase = SqlBrite.create()
        brite = briteBase.wrapDatabaseHelper(s, Schedulers.io())
        brite.setLoggingEnabled(true)
    }

    fun getApps(isDocked: Boolean): QueryObservable {
        val dockArg = (if (isDocked) 1 else 0).toString()
        return brite.createQuery(tableName, "SELECT * FROM $tableName WHERE $IS_DOCKED =? AND $IS_IGNORED = 0 ORDER BY $PACKAGE_NAME ASC", dockArg)
    }

    fun insert(vararg apps: AppDetail, overwrite : Boolean = false): List<Long> {
        val t = brite.newTransaction()
        val results : List<Long>
        try {
            val conflictAlgorithm = if (overwrite) CONFLICT_REPLACE else CONFLICT_IGNORE
            results = apps.map { brite.insert(tableName, it.toCv(), conflictAlgorithm) }
            t.markSuccessful()
        } finally {
            t.end()
        }
        return results
    }

    fun delete(appPackageName: String): Int {
        return brite.delete(tableName, "$PACKAGE_NAME = ?", appPackageName)
    }
}
