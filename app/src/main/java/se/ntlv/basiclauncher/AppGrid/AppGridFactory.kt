package se.ntlv.basiclauncher.appgrid

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.SystemClock
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import org.jetbrains.anko.*
import se.ntlv.basiclauncher.*
import se.ntlv.basiclauncher.dagger.ActivityScope
import se.ntlv.basiclauncher.database.AppDetail
import se.ntlv.basiclauncher.database.AppDetailRepository
import se.ntlv.basiclauncher.image.ImageLoaderCacher


@ActivityScope
class AppGridFactory {

    private val TAG = tag()
    private val mContext: Context
    private val mClickHandler: AppIconClickHandler
    private val mImageLoaderCacher: ImageLoaderCacher


    private var mRepo: AppDetailRepository

    constructor(context: Context,
                repo: AppDetailRepository,
                clickHandler: AppIconClickHandler,
                imageLoaderCacher: ImageLoaderCacher) {

        mContext = context
        mClickHandler = clickHandler
        mImageLoaderCacher = imageLoaderCacher
        mRepo = repo
        imageLoaderCacher.init()
    }

    fun close() {
        mImageLoaderCacher.close()
    }

    fun makePage(page: Int,
                 gridDimens: GridDimensions,
                 cellDimens: CellDimensions,
                 rows: Map<Int, List<AppDetail>>): ViewGroup {

        val sortByRow = rows.toSortedMap()

        return mContext.frameLayout {
            gridLayout {
                id = R.id.app_icons_container
                rowCount = gridDimens.rowCount
                columnCount = gridDimens.columnCount

                for (rowIdx in 0..gridDimens.rowCount - 1) {
                    val unsortedRow = sortByRow[rowIdx]
                    val row = unsortedRow?.sortedBy { it.column }
                    for (colIdx in 0..gridDimens.columnCount - 1) {
                        val app: AppDetail? = row?.find { it.column == colIdx }
                        val cell = when {
                            app != null -> {
                                Log.v(TAG, "p:$page, r:${app.row}, c:${app.column} -> ${app.packageName.toUpperCase()}")
                                makeCell(app, cellDimens)
                            }
                            else -> makePlaceHolder(page, rowIdx, colIdx, cellDimens)
                        }
                        addView(cell)

                    }
                }
            }.lparams {
                marginStart = dip(3)
                marginEnd = dip(3)
                gravity = Gravity.CENTER
            }

        }
    }

    private fun makeCell(details: AppDetail, cellDimens: CellDimensions): View {
        if (details.isPlaceholder()) throw RuntimeException("Cannot make cell from placeholder meta.")

        return mContext.frameLayout {
            appIconImageView {
                id = R.id.app_icon_view;
                scaleType = ImageView.ScaleType.FIT_CENTER
                padding = dip(10)
                loadIcon(details.packageName, mImageLoaderCacher, cellDimens.width, cellDimens.height)
                mClickHandler.bind(this, details)
                bind(AppIconDragDelegate(mRepo, details))
            }.lparams(width = cellDimens.width, height = cellDimens.height)
        }
    }

    private fun makePlaceHolder(page: Int, row: Int, column: Int, cellDimens: CellDimensions): View {
        val uniqueName = SystemClock.elapsedRealtime().toString()
        val details = AppDetail("PLACEHOLDER", uniqueName, false, false, page, row, column)
        Log.v(TAG, "p:$page, r:$row, c:$column -> PLACEHOLDER")

        return mContext.frameLayout {
            appIconImageView {
                id = R.id.app_icon_view;
                scaleType = ImageView.ScaleType.FIT_CENTER
                padding = dip(10)
                image = ColorDrawable(android.R.color.transparent)
                bind(AppIconDragDelegate(mRepo, details))
                mClickHandler.bind(this, details)
            }.lparams(width = cellDimens.width, height = cellDimens.height)

        }
    }

    fun makeDock(gridDimens: GridDimensions, cellDimens: CellDimensions, items: List<AppDetail>): ViewGroup {
        return mContext.frameLayout {
            gridLayout {
                id = R.id.app_icons_container
                rowCount = gridDimens.rowCount
                columnCount = gridDimens.columnCount

                for (rowIdx in 0..gridDimens.rowCount - 1) {
                    for (colIdx in 0..gridDimens.columnCount - 1) {
                        val app: AppDetail = items[rowIdx * gridDimens.columnCount + colIdx]
                        val cell = makeCell(app, cellDimens)
                        addView(cell)
                    }
                }
            }.lparams {
                gravity = Gravity.CENTER_HORIZONTAL
                marginStart = dip(3)
                marginEnd = dip(3)
            }
        }
    }
}
