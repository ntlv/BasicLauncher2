package se.ntlv.basiclauncher.appgrid

import android.view.View
import se.ntlv.basiclauncher.AppPageLayout
import se.ntlv.basiclauncher.CellDimensions
import se.ntlv.basiclauncher.GridDimensions
import se.ntlv.basiclauncher.database.AppDetail


class AppPageLayoutFactory {

    private val mGridFactory: AppGridFactory

    constructor(gridFactory: AppGridFactory) {

        mGridFactory = gridFactory
    }

    fun makeLayout(isDockLayout: Boolean, page : Int, controller: View.OnDragListener?, gridDimens: GridDimensions, cellDimens: CellDimensions, items: List<AppDetail>) =
            AppPageLayout(isDockLayout, page, controller, mGridFactory, items, gridDimens, cellDimens)

    fun close() {
        mGridFactory.close()
    }
}
