package ch.kra.trek.other

import android.graphics.Color

object Constants {
    const val ACTION_START_SERVICE = "ACTION_START_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
    const val ACTION_SHOW_TREK_FRAGMENT = "ACTION_SHOW_TREK_FRAGMENT"

    const val TIMER_UPDATE_INTERVAL = 50L

    const val LOCATION_UPDATE_INTERVAL = 5000L
    const val LOCATION_UPDATE_FASTEST_INTERVAL = 2000L

    const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Tracking"
    const val NOTIFICATION_ID = 1

    const val MAP_CAMERA_ZOOM = 15f
    const val POLYLINE_COLOR = Color.RED
    const val POLYLINE_WIDTH = 8f

    const val EARTH_RADIUS = 6371e3

    const val SHARED_PREFERENCES_NAME = "trekSharedPref"
    const val MAP_TYPE = "MAP_TYPE"
    const val ROAD = "ROAD"
    const val SATELLITE = "SATELLITE"
    const val HYBRID = "HYBRID"
    const val TERRAIN = "TERRAIN"
}