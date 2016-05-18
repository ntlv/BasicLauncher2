package se.ntlv.basiclauncher

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import org.jetbrains.anko.custom.ankoView
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class AppIconImageView : ImageView {
    constructor(ctx: Context) : super(ctx)

    constructor(ctx: Context, attrs: AttributeSet?) : this(ctx, attrs, 0)

    constructor(ctx: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(ctx, attrs, defStyleAttr, 0)

    constructor(ctx: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(ctx, attrs, defStyleAttr, defStyleRes)


    private var loader: Subscription? = null

    fun loadIcon(packageName: String, pm: PackageManager) {
        loader?.unsubscribe()
        loader = Observable.just(packageName)
                .subscribeOn(Schedulers.io())
                .map { it: String -> pm.getApplicationIcon(it)  }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { it: Drawable? -> setImageDrawable(it) }
    }

    fun recycle() {
        loader?.unsubscribe()
    }
}

fun android.view.ViewManager.appIconImageView(init: AppIconImageView.() -> Unit = {}) =
        ankoView({ AppIconImageView(it) }, init)

