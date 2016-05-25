package se.ntlv.basiclauncher.packagehandling

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import se.ntlv.basiclauncher.BasicLauncherApplication
import se.ntlv.basiclauncher.repository.AppDetail
import se.ntlv.basiclauncher.repository.AppDetailDB
import javax.inject.Inject


class AppChangeLoggerService : IntentService("AppChangeLoggerService") {

    val TAG = AppChangeLoggerService::class.java.simpleName

    @Inject
    lateinit var db: AppDetailDB
    @Inject
    lateinit var pM: PackageManager

    companion object {
        private val EXTRA_PACKAGE_NAME = "extra_package_name"

        private enum class Action {
            ONE_TIME_INIT,
            LOG_PACKAGE_INSTALLED,
            LOG_PACKAGE_REMOVED,
            LOG_PACKAGE_CHANGED
        }

        fun logPackageInstall(context: Context, packageName: String) {
            startLogPackageAction(context, packageName, Action.LOG_PACKAGE_INSTALLED)
        }

        fun logPackageRemove(context: Context, packageName: String) {
            startLogPackageAction(context, packageName, Action.LOG_PACKAGE_REMOVED)
        }

        fun logPackageChanged(context: Context, packageName: String) {
            startLogPackageAction(context, packageName, Action.LOG_PACKAGE_CHANGED)
        }

        private fun startLogPackageAction(context: Context, packageName: String, action: Action) {
            val intent = Intent(context, AppChangeLoggerService::class.java)
            intent.putExtra(EXTRA_PACKAGE_NAME, packageName)
            intent.action = action.name
            if (context.startService(intent) == null) {
                throw IllegalStateException("Unable to start data service")
            }
        }

        fun oneTimeInit(context: Context) {
            val i = Intent(context, AppChangeLoggerService::class.java)
            i.action = Action.ONE_TIME_INIT.name
            if (context.startService(i) == null) {
                throw IllegalStateException("Unable to start data service")
            }
        }
    }

    private val specification = Intent(Intent.ACTION_MAIN, null)
            .addCategory(Intent.CATEGORY_LAUNCHER)


    override fun onCreate() {
        super.onCreate()
        BasicLauncherApplication.applicationComponent().inject(this)
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent == null) return
        val action = Action.valueOf(intent.action)
        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
        when (action) {
            Action.LOG_PACKAGE_INSTALLED -> doLogPackageInstall(packageName)
            Action.LOG_PACKAGE_REMOVED -> doLogPackageRemove(packageName)
            Action.LOG_PACKAGE_CHANGED -> doLogPackageChange(packageName)
            Action.ONE_TIME_INIT -> doOneTimeInit()
        }
    }

    private fun doLogPackageChange(pName: String) {
        Log.d(TAG, "Log change of $pName")
        val isEnabled = pM.getApplicationEnabledSetting(pName)
        when (isEnabled) {
            PackageManager.COMPONENT_ENABLED_STATE_DEFAULT -> doLogPackageInstall(pName)
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> doLogPackageInstall(pName)
            else -> doLogPackageRemove(pName)
        }
    }

    private fun doOneTimeInit() {
        val launchIntents = pM.queryIntentActivities(specification, 0)
        launchIntents.map {
            val label = it.loadLabel(pM).toString()
            val packageName = it.activityInfo.packageName
            AppDetail(label, packageName, false, false, 0)
        }.let {
            val insertions = it.toTypedArray()
            db.insert(*insertions, overwrite = false)
        }
    }

    private fun doLogPackageInstall(pName: String) {
        Log.d(TAG, "Log installation of $pName")
        val launchable = pM.getLaunchIntentForPackage(pName)
        if (launchable == null) {
            Log.v(TAG, "Skipping logging of package install, unable to find launchable activity for $pName")
            return
        }
        val info = pM.getApplicationInfo(pName, 0)
        val label = info.loadLabel(pM).toString()
        val appDetails = AppDetail(label, pName, false, false, 0)
        db.insert(appDetails, overwrite = false)
    }

    private fun doLogPackageRemove(pName: String) {
        Log.d(TAG, "Log removal of $pName")
        db.delete(pName)
    }
}
