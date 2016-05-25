package se.ntlv.basiclauncher.appgrid

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.CheckBox
import android.widget.EditText
import org.jetbrains.anko.*
import rx.Observable
import rx.schedulers.Schedulers
import se.ntlv.basiclauncher.*
import se.ntlv.basiclauncher.repository.AppDetail
import se.ntlv.basiclauncher.repository.AppDetailRepository

class AppCellClickHandlerImpl : AppCellClickHandler {

    constructor(pm: PackageManager, act: Activity, repo: AppDetailRepository) {
        packageManager = pm
        base = act
        mRepo = repo
    }

    private val packageManager: PackageManager

    private val base: Activity

    override fun onClick(view: View, appDetail: AppDetail) {
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
    }

    private val mRepo: AppDetailRepository

    override fun onLongClick(appDetail: AppDetail): Boolean {
        Log.d("cellLongClickHandler", "Long click on $appDetail")

        val builder = AlertDialogBuilder(base)
        builder.title("Configure ${appDetail.label}")
        builder.cancellable(true)

        var ed: EditText? = null
        var cb: CheckBox? = null
        builder.customView {
            verticalLayout {
                textView {
                    text = "Current ordinal: ${appDetail.ordinal}"
                }.lparams() {
                    verticalMargin = dip(12)
                }
                ed = editText {
                    hint = "Choose an ordinal"
                }.lparams(width = matchParent, height = wrapContent) {
                    verticalMargin = dip(12)
                }
                linearLayout {
                    textView("Ignored")
                    cb = checkBox()
                }
                horizontalPadding = dip(24)
            }
        }
        builder.positiveButton("Apply", {
            var ordinal : Int? = null
            try {
                ordinal = ed?.text?.toString()?.toInt() ?: 0
            } catch (ex : NumberFormatException) { }
            val ignore = cb?.isChecked ?: false
            mRepo.updateAppDetails(appDetail.packageName, ordinal, ignore)
            dismiss()
        })
        builder.negativeButton("App settings", {
            val packageUri = Uri.parse("package:${appDetail.packageName}")
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(packageUri)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            base.startActivity(intent)
        })


        builder.show()
        return true
    }
}
