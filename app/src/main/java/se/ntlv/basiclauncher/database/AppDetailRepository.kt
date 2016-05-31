package se.ntlv.basiclauncher.database

import android.database.Cursor
import android.util.Log
import rx.Observable
import se.ntlv.basiclauncher.tag
import javax.inject.Inject

class AppDetailRepository {

    private val mDb: AppDetailDB

    @Inject
    constructor(db: AppDetailDB) {
        mDb = db
    }

    fun getPageApps() = makeAppsObserver(false, false)
    fun getDockApps() = makeAppsObserver(true, false)
    fun getIgnoredApps() = makeAppsObserver(false, true)

    private fun makeAppsObserver(isDockPage: Boolean, isIgnored : Boolean): Observable<List<AppDetail>> {
        return mDb.getApps(isDockPage, isIgnored)
                .mapToList { it: Cursor -> AppDetail(it) }
    }

    fun updateAppDetails(packageName : String, ignore: Boolean? = null, dock : Boolean? = null, page : Int? = null, row : Int? = null, column : Int? = null) {
        mDb.updatePackage(packageName, ignore, dock, page, row, column)
    }

    fun swapAppPositions(a: AppDetail, b: AppDetail) {
        Log.v(TAG, "Swapping positions for $a and $b")
        val newA = a.copy(isDock = b.isDock, isIgnored = b.isIgnored, page = b.page, row = b.row, column = b.column)
        val newB = b.copy(isDock = a.isDock, isIgnored = a.isIgnored, page = a.page, row = a.row, column = a.column)
        mDb.insert(newA, newB, overwrite = true)
    }

    private val TAG = tag()

}
