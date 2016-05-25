package se.ntlv.basiclauncher

import android.view.View
import android.view.ViewGroup
import org.jetbrains.anko.find
import org.jetbrains.anko.forEachChild
import se.ntlv.basiclauncher.appgrid.AppGridFactory
import se.ntlv.basiclauncher.repository.AppDetail


class AppDetailLayout {

    private val view: ViewGroup

    constructor (factory: AppGridFactory,
                 items: List<AppDetail>,
                 gridDimens: GridDimensions,
                 cellDimens: CellDimensions) {

        assert(items.size > gridDimens.size, { "Icons will not fit on screen." })

        view = factory.makeGrid(gridDimens, cellDimens, items)
    }

    fun getView(): ViewGroup = view

    fun unload() = view.forEachChild {
        it.find<AppIconImageView>(R.id.icon_view1).recycle()
    }
}

interface AppCellClickHandler {
    fun onClick(view: View, appDetail: AppDetail)
    fun onLongClick(appDetail: AppDetail): Boolean
}

data class GridDimensions(val rowCount: Int, val columnCount: Int) {
    val size: Int get() = rowCount * columnCount
}

data class CellDimensions(val width: Int, val height: Int)


