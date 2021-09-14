package ch.kra.trek.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import ch.kra.trek.R
import ch.kra.trek.other.Constants
import ch.kra.trek.other.Constants.ACTION_SHOW_TREK_FRAGMENT
import ch.kra.trek.other.Constants.ACTION_START_SERVICE
import ch.kra.trek.other.Constants.ACTION_STOP_SERVICE
import ch.kra.trek.other.Constants.NOTIFICATION_CHANNEL_ID
import ch.kra.trek.other.Constants.NOTIFICATION_CHANNEL_NAME
import ch.kra.trek.ui.MainActivity

class TrackingService : LifecycleService() {

    var isFirstRun = true

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_SERVICE -> {
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Log.d("service", "service already started")
                    }

                    Log.d("service", "Started service")

                }

                ACTION_STOP_SERVICE -> {
                    Log.d("service", "Stopped service")
                }

                else -> {}
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForegroundService() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false) //So that the notification stay active if the user click on it
            .setOngoing(true) //So that the notification can't be swiped away
            .setSmallIcon(R.drawable.ic_notification_trek) //to set an icon
            .setContentTitle(getString(R.string.app_name))
            .setContentText("00:00:00")
            .setContentIntent(getMainActivityPendingIntent())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            Log.d("service","oreo")
            createNotificationChannel(notificationManager)
        }
        startForeground(Constants.NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).also {
            it.action = ACTION_SHOW_TREK_FRAGMENT
        },
        FLAG_UPDATE_CURRENT
    )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }
}