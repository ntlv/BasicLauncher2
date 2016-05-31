package se.ntlv.basiclauncher.appgrid

import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import org.jetbrains.anko.onClick
import org.jetbrains.anko.onLongClick
import rx.Observable
import rx.schedulers.Schedulers
import se.ntlv.basiclauncher.*
import se.ntlv.basiclauncher.database.AppDetail
import se.ntlv.basiclauncher.database.AppDetailRepository

class AppIconClickHandler {


    constructor(pm: PackageManager, activity: Activity, repo: AppDetailRepository) {
        packageManager = pm
        base = activity
        mRepo = repo
    }

    private val packageManager: PackageManager

    private val mRepo: AppDetailRepository

    private val base: Activity

    private val TAG = tag()

    private fun onClick(view: View, appDetail: AppDetail): Boolean {
        if (appDetail.isPlaceholder()) return false
        Log.d(TAG, "Onclick $appDetail")
        val anim = AlphaAnimation(1f, 0.2f)
        anim.duration = 250
        anim.repeatMode = Animation.REVERSE
        anim.repeatCount = 1

        val animListener = listenForAnimationEvent(anim, targetEvents = AnimationEvent.END)

        val starter = Observable.just(appDetail.packageName)
                .subscribeOn(Schedulers.computation())
                .map { packageManager.getLaunchIntentForPackage(it) }

        Observable.zip(animListener, starter, pickSecond())
                .observeOn(Schedulers.computation())
                .take(1)
                .subscribe {
                    base.startActivityWithClipReveal(it, view)
                }

        view.startAnimation(anim)
        return true
    }

    private fun onLongClick(origin: View, appDetail: AppDetail): Boolean {
        if (appDetail.isPlaceholder()) return false
        Log.d(TAG, "OnLongClick $appDetail")
        val shadow = View.DragShadowBuilder(origin)
        val payload = appDetail.asClipData()
        return origin.startDrag(payload, shadow, null, 0)
    }

    fun bind(origin: View, meta: AppDetail) {
        origin.onClick { onClick(origin, meta) }
        origin.onLongClick { onLongClick(origin, meta) }
    }
}

