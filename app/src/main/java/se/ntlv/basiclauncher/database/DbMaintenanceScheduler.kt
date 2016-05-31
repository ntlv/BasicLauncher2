package se.ntlv.basiclauncher.database

import com.google.android.gms.gcm.GcmNetworkManager

class DbMaintenanceScheduler(private val manager : GcmNetworkManager) {

    fun ensureEverythingIsScheduled(scheduleTime : Long) {
        val args = DbCleaner.makeArgs(scheduleTime)
        DbCleaner.ensureScheduled(args, manager)
    }

}