package se.ntlv.basiclauncher.appgrid

import se.ntlv.basiclauncher.AppDetailLayout
import se.ntlv.basiclauncher.CellDimensions
import se.ntlv.basiclauncher.GridDimensions
import se.ntlv.basiclauncher.repository.AppDetail


class AppDetailLayoutFactory {

    private val mGridFactory: AppGridFactory

    constructor(gridFactory: AppGridFactory) {

        mGridFactory = gridFactory
    }

    fun makeLayout(items: List<AppDetail>, gridDimens: GridDimensions, cellDimens: CellDimensions) =
            AppDetailLayout(mGridFactory, items, gridDimens, cellDimens)

    fun close() {
        mGridFactory.close()
    }
}
