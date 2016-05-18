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

    fun getPageApps() = makeAppsObserver(false)
    fun getDockApps() = makeAppsObserver(true)

    private fun makeAppsObserver(isDockPage: Boolean): Observable<List<AppDetail>> {
        return mDb.getApps(isDockPage)
                .mapToList({ it: Cursor ->
                    val packageName = it.getString(0)
                    val label = it.getString(1)
                    val isDocked = it.getInt(2) == 1
                    val isIgnored = it.getInt(3) == 1
                    AppDetail(label, packageName, isDocked, isIgnored)
                })
    }

}
