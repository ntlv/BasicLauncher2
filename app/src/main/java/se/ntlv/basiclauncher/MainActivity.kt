package se.ntlv.basiclauncher

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.os.SystemClock
import android.support.v4.view.ViewPager
import android.util.DisplayMetrics
import android.util.Log
import android.view.DragEvent
import android.view.View
import android.view.ViewManager
import android.widget.FrameLayout
import android.widget.GridLayout
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.ankoView
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import se.ntlv.basiclauncher.appgrid.*
import se.ntlv.basiclauncher.dagger.ActivityComponent
import se.ntlv.basiclauncher.dagger.ActivityModule
import se.ntlv.basiclauncher.dagger.ActivityScope
import se.ntlv.basiclauncher.database.AppDetail
import se.ntlv.basiclauncher.database.AppDetailRepository
import se.ntlv.basiclauncher.database.DbMaintenanceScheduler
import se.ntlv.basiclauncher.database.GlobalConfig
import se.ntlv.basiclauncher.packagehandling.AppChangeLoggerService
import javax.inject.Inject

@ActivityScope
class MainActivity : Activity() {

    private var pager: ViewPager? = null
    private var dock: FrameLayout? = null
    private var currentDockDetails: AppPageLayout? = null

    @Inject
    lateinit var repo: AppDetailRepository

    @Inject
    lateinit var config: GlobalConfig

    @Inject
    lateinit var mFactory: AppPageLayoutFactory

    @Inject
    lateinit var mDoubleTap: DoubleTapMenuHandler

    @Inject lateinit var mScheduler: DbMaintenanceScheduler

    var pageWatch: Subscription? = null
    var dockWatch: Subscription? = null

    private fun refreshDock(apps: List<AppDetail>) {
        val cellCount = apps.size.coerceAtLeast(1)
        val dockCellWidth = (displayWidthPx - dip(12)) / cellCount

        currentDockDetails?.unload()

        val gridDimens = GridDimensions(1, apps.size)
        val cellDimens = CellDimensions(dockCellWidth, globalCellHeight)

        currentDockDetails = mFactory.makeLayout(true, 0, addToDockListener, gridDimens, cellDimens, apps)
        dock?.removeAllViews()
        dock?.addView(currentDockDetails?.getView())
    }

    private val addToDockListener = View.OnDragListener { view: View, event: DragEvent ->
        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> true
            DragEvent.ACTION_DRAG_ENTERED -> {
                view.backgroundColor = R.color.black_40
                true
            }
            DragEvent.ACTION_DRAG_LOCATION -> true
            DragEvent.ACTION_DROP -> {
                view.backgroundColor = android.R.color.transparent
                showAppMenu(this, repo, event.getAppDetails())
                true
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                view.backgroundColor = android.R.color.transparent
                true
            }
            DragEvent.ACTION_DRAG_ENDED -> true
            else -> throw IllegalArgumentException("Undefined drag action")
        }
    }

    private fun refreshPages(apps: List<AppDetail>) {
        val gridDimens = config.pageDimens
        val cellDimens = CellDimensions(pageCellWidth, globalCellHeight)

        pager?.adapter = AppAdapter(apps, gridDimens, cellDimens, mFactory, null)
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        pager?.currentItem = 0
    }

//    private var mPageController: PagerController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val start = SystemClock.elapsedRealtime()
        super.onCreate(savedInstanceState)
        ActivityComponent.init(ActivityModule(this)).inject(this)

        mScheduler.ensureEverythingIsScheduled(SystemClock.elapsedRealtime())

        if (config.shouldDoOneTimeInit) {
            config.shouldDoOneTimeInit = false
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

        pager?.let { it.setOnDragListener(PagerController(it)) }

//        mPageController = PagerController(pager!!)
        mDoubleTap.bind(pager)

        root.fitsSystemWindows = true



        Log.d("MainActivity", "Injected value: $repo")

        val pageDimens = config.pageDimens

        val dockHeight = 1
        val pageHeight = pageDimens.rowCount
        val totalHeight = dockHeight + pageHeight

        val cellInPageHorizontalCount = pageDimens.columnCount

        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        displayWidthPx = metrics.widthPixels

        pageCellWidth = (displayWidthPx / cellInPageHorizontalCount) - dip(12)

        globalCellHeight = metrics.heightPixels / totalHeight

        dockWatch = repo.getDockApps().observeOn(AndroidSchedulers.mainThread()).subscribe(
                { refreshDock(it) },
                { throw RuntimeException("GOT ERROR", it) }
        )

        pageWatch = repo.getPageApps().observeOn(AndroidSchedulers.mainThread()).subscribe (
                { refreshPages(it) },
                { throw RuntimeException("GOT ERROR", it) }
        )
        val time = SystemClock.elapsedRealtime() - start
        Log.v(TAG, "Startup time: $time")
        toast("Main create time: $time")
    }

    private val TAG = tag()
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

