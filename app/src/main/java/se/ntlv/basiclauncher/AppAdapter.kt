package se.ntlv.basiclauncher

import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import se.ntlv.basiclauncher.appgrid.AppDetailLayoutFactory
import se.ntlv.basiclauncher.repository.AppDetail
import java.util.*

class AppAdapter : PagerAdapter {

    private val apps: List<List<AppDetail>>

    private val mFactory: AppDetailLayoutFactory

    private val gridDimens : GridDimensions
    private val cellDimens : CellDimensions

    constructor(items: List<AppDetail>,
                gridDimensions: GridDimensions,
                cellDimensions: CellDimensions,
                factory: AppDetailLayoutFactory) : super() {

        gridDimens = gridDimensions
        cellDimens = cellDimensions

        mFactory = factory

        val listList: MutableList<List<AppDetail>> = ArrayList()
        var tempList: MutableList<AppDetail> = ArrayList()
        items.forEach {
            tempList.add(it)
            if (tempList.size >= gridDimens.size) {
                listList.add(tempList)
                tempList = ArrayList()
            }
        }
        if (tempList.isNotEmpty()) {
            listList.add(tempList)
        }
        apps = listList
    }

    override fun getCount(): Int = apps.size

    override fun instantiateItem(container: ViewGroup?, position: Int): Any? {
        val page = apps[position]

        val grid = mFactory.makeLayout(page, gridDimens, cellDimens)

        container?.addView(grid.getView())
        return grid
    }

    override fun destroyItem(container: ViewGroup?, position: Int, obj: Any?) {
        val grid = obj as AppDetailLayout
        grid.unload()
        container?.removeView(grid.getView())
    }

    override fun isViewFromObject(view: View?, obj: Any?): Boolean {
        val viewGroup = (obj as AppDetailLayout).getView()
        return view == viewGroup
    }
}
