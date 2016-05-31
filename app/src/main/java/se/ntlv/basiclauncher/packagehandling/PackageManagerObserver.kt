package se.ntlv.basiclauncher.packagehandling

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import se.ntlv.basiclauncher.tag


class PackageManagerObserver : BroadcastReceiver() {

    val TAG = tag()

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("PackageManagerObserver", "Received intent $intent")
        val isReplacing = intent != null && intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)
        if (isReplacing || context == null) {
            return
        }
        val packageName = intent!!.dataString
        val strippedPackageName = packageName.substringAfter(':')
        if (strippedPackageName == packageName) {
            Log.e(TAG, "Unable to parse package name of $packageName")
            return
        }
        val action = intent.action
        when (action) {
            Intent.ACTION_PACKAGE_ADDED ->
                AppChangeLoggerService.logPackageInstall(context, strippedPackageName)
            Intent.ACTION_PACKAGE_REMOVED ->
                AppChangeLoggerService.logPackageRemove(context, strippedPackageName)
            Intent.ACTION_PACKAGE_CHANGED ->
                AppChangeLoggerService.logPackageChanged(context, strippedPackageName)
            else ->
                Log.d("PackageManagerObserver", "Undefined action $action")
        }
    }
}
