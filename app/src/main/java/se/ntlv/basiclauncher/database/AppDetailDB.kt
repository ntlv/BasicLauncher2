package se.ntlv.basiclauncher.database

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
import java.io.IOException


class AppDetailDB {

    companion object {
        val version = 4
        val tableName = "app_cache"
        val IS_DOCKED = "is_docked"
        val PACKAGE_NAME = "package_name"
        val LABEL = "label"
        val IS_IGNORED = "is_ignored"
        val PAGE = "page"
        val ROW = "row"
        val COLUMN = "column"

        fun CREATE_TABLE(vararg field: String) = "create table $tableName(${field.joinToString ()}) "
    }

    private class Helper(ctx: Context) : SQLiteOpenHelper(ctx, tableName, null, version) {

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
                    "$PAGE integer not null",
                    "$ROW integer not null",
                    "$COLUMN integer not null"
            ))
        }
    }

    private val mDatabase: BriteDatabase


    constructor(ctx: Context) {
        Log.d("AppDetailDb", "CONSTRUCTING AN INSTANCE")
        val s = Helper(ctx)
        val briteDatabase = SqlBrite.create()
        mDatabase = briteDatabase.wrapDatabaseHelper(s, Schedulers.io())
        mDatabase.setLoggingEnabled(true)
    }

    /**
     * Returns a triple (Page, Row, Column) for the app with the "highest" coordinates. Do not
     * call on main thread.
     */
    fun getFirstFreeSpace() : Triple<Int, Int, Int> {
        val select = "SELECT * FROM $tableName "
        val limit = "LIMIT 1"
        val orderBy = "ORDER BY $PAGE DESC, $ROW DESC, $COLUMN DESC"
        val delimit = ";"
        val query = mDatabase.query("$select $limit $orderBy $delimit")
        if (!query.moveToFirst()) throw IOException("Can't read db or nothing found")
        val res = AppDetail(query)
        query.close()
        return Triple(res.page, res.row, res.column)
    }

    fun getApps(isDocked: Boolean, isIgnored: Boolean): QueryObservable {
        val dockArg = isDocked.toInt().toString()
        val ignoredArg = isIgnored.toInt().toString()

        val select = "SELECT * FROM $tableName "
        val where = "WHERE $IS_DOCKED = ? AND $IS_IGNORED = ?"
        val orderBy = "ORDER BY $PAGE ASC, $ROW ASC, $COLUMN ASC, $LABEL ASC"

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

    fun updatePackage(packageName: String, ignore: Boolean? = null, dock: Boolean? = null, page : Int? = null, row : Int? = null, column : Int? = null) {
        val cv = ContentValues(5)
        if (page != null) cv.put("$PAGE", page)
        if (row != null) cv.put("$ROW", row)
        if (column != null) cv.put("$COLUMN", column)
        if (ignore != null) cv.put("$IS_IGNORED", ignore.toInt())
        if (dock != null) cv.put("$IS_DOCKED", dock.toInt())
        mDatabase.update(tableName, cv, "$PACKAGE_NAME = ?", packageName)
    }
}

fun Int.toBoolean() = when {
    this == 1 -> true
    this == 0 -> false
    else -> throw IllegalArgumentException("No mapping from $this to Boolean.")
}

fun Boolean.toInt(): Int = when {
    this -> 1
    else -> 0
}
