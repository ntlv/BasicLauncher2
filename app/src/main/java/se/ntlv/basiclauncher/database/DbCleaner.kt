package se.ntlv.basiclauncher.database

import android.app.Notification
import android.os.Bundle
import android.os.SystemClock
import com.google.android.gms.gcm.*
import org.jetbrains.anko.notificationManager
import se.ntlv.basiclauncher.R
import se.ntlv.basiclauncher.tag
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

class DbCleaner() : GcmTaskService() {

    companion object {
        private val SCHEDULED_AT_KEY = "SCHEDULED_AT_KEY"

        private val TAG = tag()

        fun ensureScheduled(args: Bundle, manager: GcmNetworkManager) {
            val cleaningTask = PeriodicTask.Builder()
                    .setPeriod(TimeUnit.DAYS.toSeconds(1))
                    .setRequiredNetwork(Task.NETWORK_STATE_ANY)
                    .setRequiresCharging(false)
                    .setTag(DbCleaner.TAG)
                    .setExtras(args)
                    .setUpdateCurrent(false)
                    .setService(DbCleaner::class.java)
                    .build()
            manager.schedule(cleaningTask)
        }

        fun makeArgs(scheduledAt: Long): Bundle {
            val b = Bundle(1)
            b.putLong(SCHEDULED_AT_KEY, scheduledAt)
            return b
        }
    }


    override fun onRunTask(params: TaskParams?): Int {
        val scheduleTime = params?.extras?.get(SCHEDULED_AT_KEY) ?: 0
        val dateFormat = SimpleDateFormat.getDateTimeInstance()


        val formattedScheduleTime = dateFormat.format(scheduleTime)
        val time = SystemClock.elapsedRealtime()
        val formattedTriggerTime = dateFormat.format(time)

        val notification = Notification.Builder(this)
                .setSmallIcon(R.drawable.common_ic_googleplayservices)
                .setStyle(Notification.BigTextStyle().bigText("Scheduled at $formattedScheduleTime, triggered at $formattedTriggerTime"))
                .setContentTitle("DBCleaner")
                .setContentText("Scheduled at $scheduleTime")
                .build()

        notificationManager.notify(1, notification)

        doCleanup()

        return GcmNetworkManager.RESULT_SUCCESS
    }

    private fun doCleanup() {
        //do nothing for now
    }

}