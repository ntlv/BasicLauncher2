package se.ntlv.basiclauncher

import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import se.ntlv.basiclauncher.appgrid.AppPageLayoutFactory
import se.ntlv.basiclauncher.appgrid.PagerController
import se.ntlv.basiclauncher.database.AppDetail

class AppAdapter : PagerAdapter {

    private val apps: Map<Int, List<AppDetail>>

    private val mFactory: AppPageLayoutFactory

    private val gridDimens: GridDimensions
    private val cellDimens: CellDimensions

    private val mController: PagerController?

    constructor(items: List<AppDetail>,
                gridDimensions: GridDimensions,
                cellDimensions: CellDimensions,
                factory: AppPageLayoutFactory,
                controller: PagerController?) : super() {

        gridDimens = gridDimensions
        cellDimens = cellDimensions

        mFactory = factory

        apps = items.groupBy { it.page }

        mController = controller
    }

    override fun getCount(): Int = apps.size


    override fun instantiateItem(container: ViewGroup?, position: Int): Any? {
        val page = apps[position] ?: throw IllegalStateException("Inconsistent state detected")

        val grid = mFactory.makeLayout(false, position, mController, gridDimens, cellDimens, page)

        container?.addView(grid.getView())
        return grid
    }

    override fun destroyItem(container: ViewGroup?, position: Int, obj: Any?) {
        val grid = obj as AppPageLayout
        grid.unload()
        container?.removeView(grid.getView())
    }

    override fun isViewFromObject(view: View?, obj: Any?): Boolean {
        val viewGroup = (obj as AppPageLayout).getView()
        return view == viewGroup
    }
}
