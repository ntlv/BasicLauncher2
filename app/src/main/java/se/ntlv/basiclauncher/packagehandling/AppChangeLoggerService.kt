package se.ntlv.basiclauncher.packagehandling

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import se.ntlv.basiclauncher.BasicLauncherApplication
import se.ntlv.basiclauncher.database.AppDetail
import se.ntlv.basiclauncher.database.AppDetailDB
import se.ntlv.basiclauncher.database.GlobalConfig
import se.ntlv.basiclauncher.tag
import javax.inject.Inject


class AppChangeLoggerService : IntentService("AppChangeLoggerService") {

    val TAG = tag()

    @Inject lateinit var db: AppDetailDB
    @Inject lateinit var pM: PackageManager
    @Inject lateinit var config: GlobalConfig


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
        val dimens = config.pageDimens
        val rowsPerPage = dimens.rowCount
        val columnsPerPage = dimens.columnCount


        val pageSize = rowsPerPage * columnsPerPage

        val launchIntents = pM.queryIntentActivities(specification, 0)
                .filter {
                    val pName = it.activityInfo.packageName
                    pM.getLaunchIntentForPackage(pName) != null
                }
                .distinctBy { it.activityInfo.packageName }

        val minRequiredPageCount = Math.ceil(launchIntents.size / pageSize.toDouble()).toInt()

        val collector: MutableList<AppDetail> = mutableListOf()

        var rest = launchIntents

        for (page in 0..minRequiredPageCount - 1) {
            val take = rest.take(pageSize)
            for (row in 0..rowsPerPage - 1) {
                for (column in 0..columnsPerPage - 1) {
                    val index = row * columnsPerPage + column
                    val info = take.getOrNull(index)
                    if (info != null) {
                        val label = info.loadLabel(pM).toString()
                        val packageName = info.activityInfo.packageName
                        val meta = AppDetail(label, packageName, false, false, page, row, column)
                        Log.v(TAG, "Adding at page: ${meta.page}, row: ${meta.row}, col: ${meta.column}, ${meta.packageName} ")
                        collector.add(meta)
                    }
                }
            }
            rest = rest.drop(pageSize)
        }

        val insertions = collector.toTypedArray()
        db.insert(*insertions, overwrite = false)


/*
        launchIntents.map {
            val label = it.loadLabel(pM).toString()
            val packageName = it.activityInfo.packageName
            val res = AppDetail(label, packageName, false, false, currentPage, currentRow, currentColumn)
            currentColumn = (currentColumn + 1) % columnsPerPage
            if (currentColumn == 0) {
                currentRow = (currentRow + 1) % rowsPerPage
            }

            res
        }.let {
            val insertions = it.toTypedArray()
            db.insert(*insertions, overwrite = false)
        }*/
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
        val (page, row, column) = db.getFirstFreeSpace()
        val (maxRow, maxColumn) = config.pageDimens
        val appColumn = (column + 1) % maxColumn
        val appRow = (row + 1) % maxRow
        val appPage = page + if (appRow == 0) 1 else 0
        val appDetails = AppDetail(label, pName, false, false, appPage, appRow, appColumn)
        db.insert(appDetails, overwrite = false)
    }

    private fun doLogPackageRemove(pName: String) {
        Log.d(TAG, "Log removal of $pName")
        db.delete(pName)
    }
}
