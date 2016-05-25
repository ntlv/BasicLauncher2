package se.ntlv.basiclauncher.appgrid

import android.content.Context
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import org.jetbrains.anko.*
import se.ntlv.basiclauncher.CellDimensions
import se.ntlv.basiclauncher.GridDimensions
import se.ntlv.basiclauncher.R
import se.ntlv.basiclauncher.appIconImageView
import se.ntlv.basiclauncher.dagger.ActivityScope
import se.ntlv.basiclauncher.image.ImageLoaderCacher
import se.ntlv.basiclauncher.repository.AppDetail


@ActivityScope
class AppGridFactory {

    private val mContext: Context
    private val mClickHandler: AppCellClickHandlerImpl
    private val mImageLoaderCacher: ImageLoaderCacher


    constructor(context: Context,
                clickHandler: AppCellClickHandlerImpl,
                imageLoaderCacher: ImageLoaderCacher) {

        mContext = context
        mClickHandler = clickHandler
        mImageLoaderCacher = imageLoaderCacher
        imageLoaderCacher.init()
    }

    fun close() {
        mImageLoaderCacher.close()
    }

    fun makeGrid(gridDimens: GridDimensions, cellDimens: CellDimensions, items: List<AppDetail>): ViewGroup {
        return mContext.gridLayout {
            rowCount = gridDimens.rowCount
            columnCount = gridDimens.columnCount
            items.forEach {
                val cell = makeCell(it, cellDimens)
                addView(cell)
            }
        }
    }

    private fun makeCell(details: AppDetail, cellDimens: CellDimensions): ViewGroup {
        val layout = mContext.verticalLayout {
            id = R.id.container1
            backgroundResource = R.drawable.ripple
            gravity = Gravity.CENTER
            val image = appIconImageView {
                gravity = Gravity.CENTER
                id = R.id.icon_view1;
                scaleType = ImageView.ScaleType.FIT_CENTER
                padding = 24
            }.lparams(width = cellDimens.width, height = cellDimens.height)

            image.loadIcon(details.packageName, mImageLoaderCacher, cellDimens.width, cellDimens.height)
            onClick { mClickHandler.onClick(this, details) }
            onLongClick { mClickHandler.onLongClick(details) }
        }
        return layout
    }
}
