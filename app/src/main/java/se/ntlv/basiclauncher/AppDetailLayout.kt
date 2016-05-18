package se.ntlv.basiclauncher

import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.view.ViewPager
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import org.jetbrains.anko.*
import se.ntlv.basiclauncher.repository.AppDetail


class AppDetailLayout {

    private var apps: List<AppDetail>

    private var view: GridLayout

    constructor (pm: PackageManager,
                 items: List<AppDetail>,
                 cols: Int,
                 rows: Int,
                 context: Context,
                 cellWidth: Int,
                 cellHeight: Int,
                 onClick: (View, String) -> Unit,
                 onLongClick: (String) -> Boolean
    ) {

        apps = items
        if (apps.size > rows * cols) {
            throw IllegalStateException("Icons will not fit on screen.")
        }


        view = context.gridLayout {
            rowCount = rows
            columnCount = cols
            layoutParams = ViewPager.LayoutParams()
        }
        apps.forEach {
            val cell = makeCell(context, pm, it.packageName, cellWidth, cellHeight, onClick, onLongClick)
            view.addView(cell)
        }
    }


    fun getView(): ViewGroup {
        return view
    }

    fun unload() {
        view.forEachChild {
            (it as LinearLayout).find<AppIconImageView>(R.id.icon_view1).recycle()
        }
    }

    private fun makeCell(ctx: Context,
                         manager: PackageManager,
                         packageName: String,
                         targetWidth: Int,
                         targetHeight: Int,
                         clickHandler: (View, String) -> Unit,
                         longClickHandler: (String) -> Boolean
    ): LinearLayout {
        val layout = ctx.verticalLayout {
            id = R.id.container1
            backgroundResource = R.drawable.ripple
            gravity = Gravity.CENTER
            val image = appIconImageView {
                gravity = Gravity.CENTER
                id = R.id.icon_view1;
                scaleType = ImageView.ScaleType.FIT_CENTER
                padding = 24
            }.lparams(width = targetWidth, height = targetHeight)

            image.loadIcon(packageName, manager)
            onClick { clickHandler(this, packageName)}
            onLongClick {longClickHandler(packageName) }
        }
        return layout
    }

}


