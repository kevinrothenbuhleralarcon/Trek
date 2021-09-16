package ch.kra.trek.other

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import ch.kra.trek.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object TrackingUtility {

    /*fun hasLocationPermission(context: Context) =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } else {
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }*/

    fun hasLocationPermission(context: Context) =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else {
            context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED /*&&
                    context.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED*/
        }

    fun requestPermissions(activity: Activity, context: Context, permissions: List<String>, requestPermissionLauncher: ActivityResultLauncher<Array< String>>) {

        if (!checkPermissions(context, permissions)){
            if (requireContext(activity, permissions)){
                MaterialAlertDialogBuilder(context)
                    .setTitle(R.string.dialog_permission_location_title)
                    .setMessage(R.string.dialog_permission_location_message)
                    .setNeutralButton(R.string.dialog_permission_location_btn_neutral_text) { _, _ ->
                        requestPermissionLauncher.launch(
                            permissions.toTypedArray()
                        )
                    }
                    .show()
            }
        } else {
            requestPermissionLauncher.launch(
                permissions.toTypedArray()
            )
        }
    }

    private fun checkPermissions(context: Context, permissions: List<String>): Boolean {
        for (permission in permissions) {
            if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    private fun requireContext(activity: Activity, permissions: List<String>): Boolean {
        for (permission in permissions) {
            if (activity.shouldShowRequestPermissionRationale(permission)) {
                return true
            }
        }
        return false
    }
}