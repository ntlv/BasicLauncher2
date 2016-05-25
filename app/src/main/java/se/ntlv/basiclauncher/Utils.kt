package se.ntlv.basiclauncher

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.animation.Animation
import rx.Observable
import rx.Subscriber


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

fun Context.startActivityWithClipReveal(intent : Intent, originateAt : View) {
    val originY = originateAt.height / 2
    val originX = originateAt.width / 2
    val startOptions = ActivityOptions.makeClipRevealAnimation(originateAt, originX, originY, 10, 10)

    startActivity(intent, startOptions.toBundle())
}

fun <A, B> pickSecond() = { a: A, b: B -> b }



