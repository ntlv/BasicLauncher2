package se.ntlv.basiclauncher

import android.content.pm.PackageManager
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import se.ntlv.basiclauncher.repository.AppDetail
import java.util.*

class AppAdapter : PagerAdapter {

    private val apps: List<List<AppDetail>>

    private val mRows: Int
    private val mCols: Int

    private var mCellWidth: Int
    private var mCellHeight: Int

    private val mOnClick: (View, String) -> Unit
    private val mOnLongClick: (String) -> Boolean

    private val mPm: PackageManager

    constructor(pm : PackageManager,
                items: List<AppDetail>,
                rows: Int, cols: Int,
                cellWidth: Int,
                cellHeight: Int,
                onClick: (View, String) -> Unit,
                onLongClick: (String) -> Boolean
                ) : super() {
        mRows = rows
        mCols = cols
        mCellWidth = cellWidth
        mCellHeight = cellHeight

        val listList: MutableList<List<AppDetail>> = ArrayList()
        var tempList: MutableList<AppDetail> = ArrayList()
        items.forEach {
            tempList.add(it)
            if (tempList.size >= mRows * mCols) {
                listList.add(tempList)
                tempList = ArrayList()
            }
        }
        if (tempList.isNotEmpty()) {
            listList.add(tempList)
        }
        apps = listList

        mPm = pm
        mOnClick = onClick
        mOnLongClick = onLongClick
    }

    override fun getCount(): Int = apps.size

    override fun instantiateItem(container: ViewGroup?, position: Int): Any? {

        val context = container?.context ?: throw IllegalStateException("Need valid context to do things")
        val page = apps[position]
        val grid = AppDetailLayout(mPm, page, mCols, mRows + 1, context, mCellWidth, mCellHeight, mOnClick, mOnLongClick)
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
