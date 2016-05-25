package se.ntlv.basiclauncher

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.support.v4.view.ViewPager
import android.util.DisplayMetrics
import android.util.Log
import android.view.ViewManager
import android.widget.FrameLayout
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.verticalLayout
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import se.ntlv.basiclauncher.appgrid.AppDetailLayoutFactory
import se.ntlv.basiclauncher.appgrid.DoubleTapMenuHandler
import se.ntlv.basiclauncher.dagger.ActivityComponent
import se.ntlv.basiclauncher.dagger.ActivityModule
import se.ntlv.basiclauncher.dagger.ActivityScope
import se.ntlv.basiclauncher.packagehandling.AppChangeLoggerService
import se.ntlv.basiclauncher.repository.AppDetail
import se.ntlv.basiclauncher.repository.AppDetailRepository
import javax.inject.Inject

@ActivityScope
class MainActivity : Activity(), AnkoLogger {

    private var pager: ViewPager? = null
    private var dock: FrameLayout? = null
    private var currentDockDetails: AppDetailLayout? = null

    @Inject
    lateinit var repo: AppDetailRepository
    @Inject
    lateinit var prefs: SharedPreferences

    @Inject
    lateinit var mFactory: AppDetailLayoutFactory

    @Inject
    lateinit var mDoubleTap : DoubleTapMenuHandler

    private val KEY_ONE_TIME_INIT = "key_one_time_init"

    var pageWatch: Subscription? = null
    var dockWatch: Subscription? = null

    private fun refreshDock(apps: List<AppDetail>) {
        val cellCount = apps.size.coerceAtLeast(1)
        val dockCellWidth = displayWidthPx / cellCount

        currentDockDetails?.unload()

        val gridDimens = GridDimensions(1, apps.size)
        val cellDimens = CellDimensions(dockCellWidth, globalCellHeight)

        currentDockDetails = mFactory.makeLayout(apps, gridDimens, cellDimens)
        dock?.removeAllViews()
        dock?.addView(currentDockDetails?.getView())
    }

    private fun refreshPages(apps: List<AppDetail>) {
        val gridDimens = GridDimensions(5, 4)
        val cellDimens = CellDimensions(pageCellWidth, globalCellHeight)

        pager?.adapter = AppAdapter(apps, gridDimens, cellDimens, mFactory)
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        pager?.currentItem = 0
    }

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityComponent.Init.init(ActivityModule(this)).inject(this)

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

        mDoubleTap.bind(pager)


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

        dockWatch = repo.getDockApps().observeOn(AndroidSchedulers.mainThread()).subscribe(
                { refreshDock(it) },
                { throw RuntimeException("GOT ERROR", it) }
        )

        pageWatch = repo.getPageApps().observeOn(AndroidSchedulers.mainThread()).subscribe (
                { refreshPages(it) },
                { throw RuntimeException("GOT ERROR", it) }
        )

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
        mFactory.close()
    }

    override fun onBackPressed() {
        /*do nothing, this is home activity */
    }
}

