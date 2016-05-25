package se.ntlv.basiclauncher

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import org.jetbrains.anko.custom.ankoView
import rx.Subscription
import se.ntlv.basiclauncher.image.ImageLoaderCacher

class AppIconImageView : ImageView {
    constructor(ctx: Context) : super(ctx)

    constructor(ctx: Context, attrs: AttributeSet?) : this(ctx, attrs, 0)

    constructor(ctx: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(ctx, attrs, defStyleAttr, 0)

    constructor(ctx: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(ctx, attrs, defStyleAttr, defStyleRes)


    private var loader: Subscription? = null

    fun loadIcon(packageName: String, imageLoaderCacher: ImageLoaderCacher, width: Int, height: Int) {
        loader?.unsubscribe()
        loader = imageLoaderCacher.loadImage(packageName, this, width, height)

    }

    fun recycle() {
        loader?.unsubscribe()
    }
}

fun android.view.ViewManager.appIconImageView(init: AppIconImageView.() -> Unit = {}) =
        ankoView({ AppIconImageView(it) }, init)

