package ch.kra.trek.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import ch.kra.trek.R
import ch.kra.trek.database.Coordinate
import ch.kra.trek.helper.Chrono
import ch.kra.trek.other.Constants.ACTION_SHOW_TREK_FRAGMENT
import ch.kra.trek.other.Constants.ACTION_START_SERVICE
import ch.kra.trek.other.Constants.ACTION_STOP_SERVICE
import ch.kra.trek.other.Constants.LOCATION_UPDATE_FASTEST_INTERVAL
import ch.kra.trek.other.Constants.LOCATION_UPDATE_INTERVAL
import ch.kra.trek.other.Constants.NOTIFICATION_CHANNEL_ID
import ch.kra.trek.other.Constants.NOTIFICATION_CHANNEL_NAME
import ch.kra.trek.other.Constants.NOTIFICATION_ID
import ch.kra.trek.other.TrackingUtility
import ch.kra.trek.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import java.text.SimpleDateFormat
import java.util.*

class TrackingService : LifecycleService() {

    private var isFirstRun = true
    private var serviceKilled = false
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var currentNotificationBuilder : NotificationCompat.Builder
    private val chrono = Chrono()

    companion object {
        val isTracking = MutableLiveData<Boolean>()
        val coordinates = MutableLiveData<MutableList<Coordinate>>()
        val timeInMs = MutableLiveData<Long>()
    }

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if (isTracking.value!!) {
                result?.locations?.let { locations ->
                    for (location in locations) {
                        addLocation(location)
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        currentNotificationBuilder = getBaseNotificationBuilder()
        postInitialValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        isTracking.observe(this, {
            updateLocationTracking(it)
        })
        chrono.timeInMs.observe(this) {
            timeInMs.postValue(it)
        }
    }

    private fun postInitialValues() {
        isTracking.postValue(false)
        coordinates.postValue(mutableListOf())
        timeInMs.postValue(0L)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_SERVICE -> {
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {
                    }

                }

                ACTION_STOP_SERVICE -> {
                    killService()
                }

                else -> {}
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForegroundService() {
        chrono.startTimer()
        isTracking.postValue(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createNotificationChannel(notificationManager)
        }
        startForeground(NOTIFICATION_ID, getBaseNotificationBuilder().build())
        chrono.timeInS.observe(this) {
            val dateFormat = SimpleDateFormat("HH:mm:ss")
            dateFormat.timeZone = TimeZone.getTimeZone("GMT+0") //needed on physical device for a chrono in order that it start at 0
            val notification = currentNotificationBuilder
                .setContentText(dateFormat.format(it * 1000))
            notificationManager.notify(NOTIFICATION_ID, notification.build())
        }
    }

    private fun killService() {
        serviceKilled = true
        isFirstRun = true
        isTracking.postValue(false)
        chrono.stopTimer()
        postInitialValues()
        updateLocationTracking(isTracking.value!!)
        stopForeground(true)
        stopSelf()
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

    private fun addLocation(location: Location?) {
        location?.let {
            coordinates.value?.apply {
                add(Coordinate(it.latitude, it.longitude, it.altitude))
                //add(Coordinate(it.latitude, it.longitude, Random.nextDouble(0.0, 700.0)))
                coordinates.postValue(this)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtility.hasLocationPermission(this)) {
                val request = LocationRequest.create().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = LOCATION_UPDATE_FASTEST_INTERVAL
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    private fun getBaseNotificationBuilder(): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false) //So that the notification stay active if the user click on it
            .setOngoing(true) //So that the notification can't be swiped away
            .setSmallIcon(R.drawable.ic_notification_trek) //to set an icon
            .setContentTitle(getString(R.string.app_name))
            .setContentText("00:00:00")
            .setContentIntent(getMainActivityPendingIntent())
    }
}