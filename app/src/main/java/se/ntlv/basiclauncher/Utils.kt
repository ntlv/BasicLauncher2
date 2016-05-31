package se.ntlv.basiclauncher

import android.app.ActivityOptions
import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.DragEvent
import android.view.View
import android.view.animation.Animation
import android.widget.CheckBox
import org.jetbrains.anko.*
import rx.Observable
import rx.Subscriber
import se.ntlv.basiclauncher.database.AppDetail
import se.ntlv.basiclauncher.database.AppDetailRepository

private val DRAG_EVENT_APP_DETAIL_FIELD = "meta"

fun showAppMenu(context: Context, repository: AppDetailRepository, appDetail: AppDetail): Boolean {


    val builder = AlertDialogBuilder(context)
    builder.title("Configure ${appDetail.label}")
    builder.cancellable(true)

    var ignoreCheckBox: CheckBox? = null
    var dockCheckBox: CheckBox? = null
    builder.customView {
        verticalLayout {
            ignoreCheckBox = checkBox("Ignored")
            dockCheckBox = checkBox("Docked") {
                isChecked = appDetail.isDock
            }
            horizontalPadding = dip(24)
        }
    }
    builder.positiveButton("Apply", {
        val ignore = ignoreCheckBox?.isChecked ?: false
        val dock = dockCheckBox?.isChecked ?: false
        repository.updateAppDetails(appDetail.packageName, ignore, dock)
        dismiss()
    })
    builder.negativeButton("App settings", {
        val packageUri = Uri.parse("package:${appDetail.packageName}")
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(packageUri)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        context.startActivity(intent)
    })


    builder.show()
    return true

}



fun i2e(event: DragEvent?): String {
    return when (event?.action) {
        null -> "null"
        DragEvent.ACTION_DRAG_STARTED -> "started"
        DragEvent.ACTION_DRAG_ENTERED -> "entered"
        DragEvent.ACTION_DRAG_LOCATION -> "location"
        DragEvent.ACTION_DRAG_EXITED -> "exited"
        DragEvent.ACTION_DRAG_ENDED -> "ended"
        else -> "<<<${event?.action}>>>"
    }
}

fun AppDetail.asClipData(): ClipData {
    val intent = Intent()
    intent.putExtra(DRAG_EVENT_APP_DETAIL_FIELD, this)
    val data = ClipData.Item(intent)
    return ClipData(label, arrayOf(ClipDescription.MIMETYPE_TEXT_INTENT), data)
}

fun DragEvent.getAppDetails(): AppDetail {
    val base = clipData.getItemAt(0).intent.extras
    base.classLoader = AppDetail::class.java.classLoader
    val unCast = base.get(DRAG_EVENT_APP_DETAIL_FIELD)
    return unCast as AppDetail
}

enum class AnimationEvent {
    START,
    REPEAT,
    END
}

/**
 * Create an observable that emits an [AnimationEvent] whenever the
 * passed in animation is started, repeats or ends. Once an [AnimationEvent.END]
 * has been emitted the observable will emit a completion event. It is probably
 * a good idea to subscribe to this observable before the animation is started since
 * the observable will not begin observing the animation before it is subscribed to.
 *
 * @param anim The animation that should be observed.
 * @param targetEvents Animation events that should be observed. Defaults to all events i.e all [AnimationEvent]
 * @return A observable the upon being subscribed to will begin observing the [anim]
 * and forward the animation events to the observable's subscribers.
 *
 */
fun listenForAnimationEvent(anim: Animation,
                            vararg targetEvents: AnimationEvent = AnimationEvent.values()): Observable<AnimationEvent> {
    val obs = Observable.create<AnimationEvent>({ s: Subscriber<in AnimationEvent> ->
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(animation: Animation?) {
                s.onNext(AnimationEvent.END)
                s.onCompleted()
            }

            override fun onAnimationStart(animation: Animation?) {
                s.onNext(AnimationEvent.START)
            }

            override fun onAnimationRepeat(animation: Animation?) {
                s.onNext(AnimationEvent.REPEAT)
            }

        })
    }).filter { it in targetEvents }
    return obs
}

fun Context.startActivityWithClipReveal(intent: Intent, originateAt: View) {
    val originY = originateAt.height / 2
    val originX = originateAt.width / 2
    val startOptions = ActivityOptions.makeClipRevealAnimation(originateAt, originX, originY, 10, 10)

    startActivity(intent, startOptions.toBundle())
}

fun <A, B> pickSecond() = { a: A, b: B -> b }

inline fun <reified T : Any> T.tag() = javaClass.simpleName

