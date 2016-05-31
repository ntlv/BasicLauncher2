package se.ntlv.basiclauncher

import android.view.View
import android.view.ViewGroup
import org.jetbrains.anko.find
import org.jetbrains.anko.forEachChild
import se.ntlv.basiclauncher.appgrid.AppGridFactory
import se.ntlv.basiclauncher.database.AppDetail


class AppPageLayout {

    private val view: ViewGroup

    constructor (isDockLayout: Boolean,
                 page: Int,
                 controller: View.OnDragListener?,
                 factory: AppGridFactory,
                 items: List<AppDetail>,
                 gridDimens: GridDimensions,
                 cellDimens: CellDimensions) {

        assert(items.size > gridDimens.size, { "Icons will not fit on screen." })

        view = if (!isDockLayout) {

            val rows = items.groupBy { it.row }

            factory.makePage(page, gridDimens, cellDimens, rows)
        } else {
            factory.makeDock(gridDimens, cellDimens, items)
        }
        controller?.let { view.setOnDragListener(it) }

    }

    fun getView(): ViewGroup = view

    fun unload() = view.find<ViewGroup>(R.id.app_icons_container).forEachChild {
        it.find<AppIconImageView>(R.id.app_icon_view).recycle()
    }
}

data class GridDimensions(val rowCount: Int, val columnCount: Int) {
    val size: Int get() = rowCount * columnCount
}

data class CellDimensions(val width: Int, val height: Int)


