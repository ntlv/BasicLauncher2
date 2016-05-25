package se.ntlv.basiclauncher.appgrid

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import org.jetbrains.anko.AlertDialogBuilder
import rx.android.schedulers.AndroidSchedulers
import se.ntlv.basiclauncher.repository.AppDetailRepository

class DoubleTapMenuHandler {


    private val mDetector: GestureDetector
    private val mDb: AppDetailRepository
    private val mContext : Context

    constructor(context: Context, db: AppDetailRepository) {
        val simpleDetector = object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent?): Boolean {
                showMenu()
                return true
            }
        }
        mDb = db
        mContext = context
        mDetector = GestureDetector(context, simpleDetector)
    }

    private fun showMenu() {
        mDb.getIgnoredApps()
                .take(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val apps = it
                    val builder = AlertDialogBuilder(mContext)
                    builder.title("Restore ignored app")
                    builder.items(it.map { it.label }, {
                        mDb.updateAppDetails(apps[it].packageName, ignore = false)
                    })
                    builder.show()
                }
    }

    fun bind(eventOrigin: View?) {
        eventOrigin?.setOnTouchListener { view, event -> mDetector.onTouchEvent(event) }
    }
}


