package ch.kra.trek.other

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager

object TrackingUtility {
    fun hasLocationPermission(context: Context) =
            context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
}