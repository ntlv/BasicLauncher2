package se.ntlv.basiclauncher.repository

import android.database.Cursor
import rx.Observable
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

    fun updateAppDetails(packageName : String, ordinal: Int? = null, ignore: Boolean? = null) {
        mDb.updatePackage(packageName, ordinal, ignore)
    }

}
