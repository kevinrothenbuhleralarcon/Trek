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
import ch.kra.trek.other.Constants
import ch.kra.trek.other.Constants.ACTION_SHOW_TREK_FRAGMENT
import ch.kra.trek.other.Constants.ACTION_START_SERVICE
import ch.kra.trek.other.Constants.ACTION_STOP_SERVICE
import ch.kra.trek.other.Constants.LOCATION_UPDATE_FASTEST_INTERVAL
import ch.kra.trek.other.Constants.LOCATION_UPDATE_INTERVAL
import ch.kra.trek.other.Constants.NOTIFICATION_CHANNEL_ID
import ch.kra.trek.other.Constants.NOTIFICATION_CHANNEL_NAME
import ch.kra.trek.other.TrackingUtility
import ch.kra.trek.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng

class TrackingService : LifecycleService() {

    private var isFirstRun = true
    private var serviceKilled = false
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var timeStart = 0L
    private var timeEnd = 0L

    companion object {
        val isTracking = MutableLiveData<Boolean>()
        val pathPoint = MutableLiveData<MutableList<LatLng>>()
        val altitudes = MutableLiveData<MutableList<Double>>()
        val timeInMs = MutableLiveData<Long>()
    }

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if (isTracking.value!!) {
                result?.locations?.let { locations ->
                    for (location in locations) {
                        addPathPoint(location)
                        addAltitude(location)
                        postTime()
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        postInitialValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        isTracking.observe(this, {
            updateLocationTracking(it)
        })
    }

    private fun postInitialValues() {
        isTracking.postValue(false)
        pathPoint.postValue(mutableListOf())
        altitudes.postValue(mutableListOf())
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
        isTracking.postValue(true)
        startChrono()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false) //So that the notification stay active if the user click on it
            .setOngoing(true) //So that the notification can't be swiped away
            .setSmallIcon(R.drawable.ic_notification_trek) //to set an icon
            .setContentTitle(getString(R.string.app_name))
            .setContentText("00:00:00")
            .setContentIntent(getMainActivityPendingIntent())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createNotificationChannel(notificationManager)
        }
        startForeground(Constants.NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun killService() {
        serviceKilled = true
        isFirstRun = true
        isTracking.postValue(false)
        stopChrono()
        postTime()
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

    private fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(it.latitude, it.longitude)
            pathPoint.value?.apply {
                add(pos)
                pathPoint.postValue(this)
            }
        }
    }

    private fun addAltitude(location: Location?) {
        location?.let {
            altitudes.value?.apply {
                add(location.altitude)
                altitudes.postValue(this)
            }
        }
    }

    private fun postTime() {
        timeInMs.postValue(deltaTime())
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

    private fun startChrono() {
        timeEnd = -1L
        timeStart = System.currentTimeMillis()
    }

    private fun stopChrono() { timeEnd = System.currentTimeMillis() }

    private fun deltaTime(): Long {
        return if (timeEnd != -1L) {
            timeEnd - timeStart
        } else {

            System.currentTimeMillis() - timeStart
        }
    }
}