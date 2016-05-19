package se.ntlv.basiclauncher

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.support.v4.view.ViewPager
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewManager
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.FrameLayout
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.verticalLayout
import rx.Observable
import rx.Subscriber
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import se.ntlv.basiclauncher.packagehandling.AppChangeLoggerService
import se.ntlv.basiclauncher.repository.AppDetail
import se.ntlv.basiclauncher.repository.AppDetailRepository
import javax.inject.Inject


class MainActivity : Activity(), AnkoLogger {

    private var pager: ViewPager? = null
    private var dock: FrameLayout? = null
    private var currentDockDetails: AppDetailLayout? = null

    @Inject lateinit var repo: AppDetailRepository
    @Inject lateinit var prefs: SharedPreferences
    @Inject lateinit var mPackageManager: PackageManager

    private val KEY_ONE_TIME_INIT = "key_one_time_init"

    var pageWatch: Subscription? = null
    var dockWatch: Subscription? = null

    val onClick: (View, String) -> Unit = { view: View, packageName: String ->
        val anim = AlphaAnimation(1f, 0.2f)
        anim.duration = 250
        anim.repeatMode = Animation.REVERSE
        anim.repeatCount = 1

        val animListener = makeAnimationListenerObservable(anim, AnimationEvent.END)

        val starter = Observable.just(packageName)
                .subscribeOn(Schedulers.computation())
                .map { mPackageManager.getLaunchIntentForPackage(it) }

        Observable.zip(animListener, starter, pickSecond())
                .observeOn(Schedulers.computation())
                .subscribe {
                    val originY = view.height / 2
                    val originX = view.width / 2
                    val startOptions = ActivityOptions.makeClipRevealAnimation(view, originX, originY, 0, 0)

                    startActivity(it, startOptions.toBundle())
                }

        view.startAnimation(anim)
    }

    fun <A, B> pickSecond() = {a : A, b : B  -> b}


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
    fun makeAnimationListenerObservable(anim: Animation,
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

    val TAG = MainActivity::class.java.simpleName

    val onLongCLick: (String) -> Boolean = {
        Log.d(TAG, "Long click on $it")
        val packageUri = Uri.parse("package:$it")
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(packageUri)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        startActivity(intent)
        true
    }

    private fun refreshAppList(isDock: Boolean, apps: List<AppDetail>) {
        if (isDock) {
            val cellCount = apps.size.coerceAtLeast(1)
            val dockCellWidth = displayWidthPx / cellCount

            currentDockDetails?.unload()
            currentDockDetails = AppDetailLayout(mPackageManager, apps, apps.size, 1, this@MainActivity, dockCellWidth, globalCellHeight, onClick, onLongCLick)
            dock?.removeAllViews()
            dock?.addView(currentDockDetails?.getView())
        } else {
            pager?.adapter = AppAdapter(mPackageManager, apps, 5, 4, pageCellWidth, globalCellHeight, onClick, onLongCLick)
        }
    }


    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        BasicLauncherApplication.graph.inject(this)
        val oneTimeInitCompleted = prefs.getBoolean(KEY_ONE_TIME_INIT, false)
        if (oneTimeInitCompleted.not()) {
            prefs.edit().putBoolean(KEY_ONE_TIME_INIT, true).apply()
            AppChangeLoggerService.oneTimeInit(this)
        }


        val root = verticalLayout {

            pager = viewPager {
                id = R.id.view_pager_main

            }.lparams(width = matchParent, height = 0, weight = 6f)
            dock = frameLayout {
                id = R.id.dock
            }.lparams(width = matchParent, height = 0, weight = 1f)
        }
        root.fitsSystemWindows = true

        Log.d("MainActivity", "Injected value: $repo")

        val dockHeight = 1
        val pageHeight = 5
        val totalHeight = dockHeight + pageHeight

        val cellInPageHorizontalCount = 4

        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        displayWidthPx = metrics.widthPixels

        pageCellWidth = displayWidthPx / cellInPageHorizontalCount


        globalCellHeight = metrics.heightPixels / totalHeight

        dockWatch = repo.getDockApps().observeOn(AndroidSchedulers.mainThread()).subscribe({
            refreshAppList(true, it)
        }, {
            throw RuntimeException("GOT ERROR", it)
        })
        pageWatch = repo.getPageApps().observeOn(AndroidSchedulers.mainThread()).subscribe ({
            refreshAppList(false, it)
        }, {
            throw RuntimeException("GOT ERROR", it)
        })

    }

    private var pageCellWidth = 0
    private var globalCellHeight = 0
    private var displayWidthPx = 0


    fun ViewManager.viewPager(init: ViewPager.() -> Unit = {}) =
            ankoView({ ViewPager(it) }, init)

    override fun onDestroy() {
        super.onDestroy()
        pageWatch?.unsubscribe()
        dockWatch?.unsubscribe()
    }

    override fun onBackPressed() {
        /*do nothing, this is home activity */
    }
}

