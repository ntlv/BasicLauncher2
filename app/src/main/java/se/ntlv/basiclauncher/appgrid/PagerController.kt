package se.ntlv.basiclauncher.appgrid

import android.graphics.Rect
import android.os.SystemClock
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.DragEvent
import android.view.View
import android.widget.GridLayout
import org.jetbrains.anko.dip
import org.jetbrains.anko.find
import org.jetbrains.anko.forEachChild
import org.jetbrains.anko.onLayoutChange
import se.ntlv.basiclauncher.R
import se.ntlv.basiclauncher.i2e
import se.ntlv.basiclauncher.tag
import kotlin.comparisons.compareValues

class PagerController : View.OnDragListener {
    override fun onDrag(view: View, event: DragEvent): Boolean {
        return handleDrag(view, event)
    }

    private val TAG = tag()

    private val mPager: ViewPager
    private val tolerance: Int

    private var mRight: Int? = null
    private var mLeft: Int? = null

    private var mLatestScroll = 0L

    constructor(pager: ViewPager) {
        mPager = pager
        val listener = { view: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int ->
            mRight = right
            mLeft = left

        }
        mPager.onLayoutChange(listener)
        tolerance = pager.context.dip(12)
    }

    private fun handleDrag(view: View, dragEvent: DragEvent): Boolean {
        Log.v(TAG, "${view.javaClass.simpleName} got ${i2e(dragEvent)} at ${dragEvent.x}, ${dragEvent.y}")
        when (dragEvent.action) {
            DragEvent.ACTION_DRAG_LOCATION -> {

                val x = dragEvent.x
                val y = dragEvent.y

                if (800 < SystemClock.elapsedRealtime() - mLatestScroll) {

                    val rightTolerancePoint = mRight?.minus(tolerance)?.toFloat() ?: x
                    val leftTolerancePoint = mLeft?.plus(tolerance)?.toFloat() ?: x
                    Log.e(TAG, "rTol: $rightTolerancePoint, lTol: $leftTolerancePoint, x,y: $x,$y")

                    when {
                        x < leftTolerancePoint -> {
                            val current = mPager.currentItem
                            val next = max(0, current - 1)
                            Log.v(TAG, "Trying to page LEFT, $current -> $next")
                            mPager.currentItem = next
                            mLatestScroll = SystemClock.elapsedRealtime()
                            return true
                        }
                        x > rightTolerancePoint -> {
                            val current = mPager.currentItem
                            val lastPage = mPager.adapter.count - 1
                            val newPage = min(lastPage, current + 1)
                            Log.v(TAG, "Trying to page RIGHT, $current -> $newPage")
                            mPager.currentItem = newPage
                            mLatestScroll = SystemClock.elapsedRealtime()
                            return true
                        }
                    }
                }

                val r = Rect()

                mPager.find<GridLayout>(R.id.app_icons_container).forEachChild { child: View ->
                    r.left = child.left
                    r.top = child.top
                    r.right = child.right
                    r.bottom = child.bottom
                    Log.v(TAG, "Testing if $r contains $x, $y")
                    if (r.contains(x.toInt(), y.toInt())) {
                        Log.v(TAG, "Sending drag event to $child")
                        if (child.onDragEvent(dragEvent)) {
                            Log.e(TAG, "Hit ${child.javaClass.simpleName}.${child.hashCode()} at $r, $x, $y")
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    fun <T : Comparable<*>> max(a: T, b: T): T {
        val res = compareValues(a, b)
        return if (res < 0) b else a
    }

    fun <T : Comparable<*>> min(a: T, b: T): T {
        val res = compareValues(a, b)
        return if (res > 0) b else a
    }
}