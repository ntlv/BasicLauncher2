package se.ntlv.basiclauncher.appgrid

import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.DragEvent
import android.view.View
import se.ntlv.basiclauncher.R
import se.ntlv.basiclauncher.database.AppDetail
import se.ntlv.basiclauncher.database.AppDetailRepository
import se.ntlv.basiclauncher.getAppDetails
import se.ntlv.basiclauncher.i2e
import se.ntlv.basiclauncher.tag


class AppIconDragDelegate(private val mRepo: AppDetailRepository, private val mAppDetails: AppDetail) {

    fun onDragEvent(view: View, arg: DragEvent): Boolean {
        Log.d(TAG, "got event ${i2e(arg)}")
        return when (arg.action) {
            DragEvent.ACTION_DRAG_STARTED -> true
            DragEvent.ACTION_DRAG_ENTERED -> {
                view.indicateCanReceive(mAppDetails.isPlaceholder());true
            }
            DragEvent.ACTION_DRAG_LOCATION -> true
            DragEvent.ACTION_DROP -> {
                view.receiveDrop(arg, mAppDetails)
                view.stopIndicateCanReceive(mAppDetails.isPlaceholder())
                true
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                view.stopIndicateCanReceive(mAppDetails.isPlaceholder()); true
            }
            DragEvent.ACTION_DRAG_ENDED -> {
                Log.e(TAG, "${mAppDetails.label} got drag end.")
                true
            }
            else -> throw IllegalArgumentException("Undefined drag action")
        }
    }

    private val TAG = tag()

    fun View.receiveDrop(theDrop: DragEvent, self: AppDetail) {
        val payload = theDrop.getAppDetails()
        Log.v(TAG, "${this.hashCode()}{{${self.label}}} RECEIVED $payload")
        if (payload.isPlaceholder()) {
            throw IllegalArgumentException("Received placeholder as payload!")
        }
        if (self.isPlaceholder()) {
            mRepo.updateAppDetails(payload.packageName, page = self.page, row = self.row, column = self.column)
        } else {
            mRepo.swapAppPositions(payload, self)
        }
    }

    fun View.indicateCanReceive(animateBackground: Boolean): Unit {
        if (animateBackground) {
            background = ColorDrawable(R.color.black_40)
            animate().scaleX(1.5f).scaleY(1.5f).alpha(1f)
        } else {
            animate().scaleX(1.5f).scaleY(1.5f)
        }
    }

    fun View.stopIndicateCanReceive(animateBackground: Boolean): Unit {
        if (animateBackground) {
            animate().scaleX(1.0f).scaleY(1.0f).alpha(0f).withEndAction {
                background = ColorDrawable(android.R.color.transparent)
            }
        } else {
            animate().scaleX(1.0f).scaleY(1.0f)
        }
    }
}