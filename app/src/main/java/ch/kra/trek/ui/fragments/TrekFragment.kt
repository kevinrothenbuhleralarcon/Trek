package ch.kra.trek.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ch.kra.trek.R
import ch.kra.trek.TrekApplication
import ch.kra.trek.database.Coordinate
import ch.kra.trek.databinding.FragmentTrekBinding
import ch.kra.trek.other.Constants
import ch.kra.trek.other.Constants.MAP_CAMERA_ZOOM
import ch.kra.trek.other.Constants.POLYLINE_COLOR
import ch.kra.trek.other.Constants.POLYLINE_WIDTH
import ch.kra.trek.services.TrackingService
import ch.kra.trek.ui.viewmodels.TrekViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*

class TrekFragment : Fragment() {

    @SuppressLint("MissingPermission")
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (it[Manifest.permission.ACCESS_FINE_LOCATION] == true || it[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                //permission granted
            } else {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.dialog_permission_location_title))
                    .setMessage(getString(R.string.dialog_permission_location_message))
                    .setNeutralButton(getString(R.string.dialog_permission_location_btn_neutral_text)) { _, _ ->
                        activity?.onBackPressed()
                    }
                    .show()
            }
        }

    private val viewModel: TrekViewModel by activityViewModels {
        TrekViewModel.TrekViewModelFactory((activity?.application as TrekApplication).database.trekDao())
    }

    private var map: GoogleMap? = null

    private var _binding: FragmentTrekBinding? = null
    private val binding get() = _binding!!

    private var isTracking = false
    private var coordinates = mutableListOf<Coordinate>()
    private var timeInMs = 0L
    private var polyLine: Polyline? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_trek, container, false)
        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermissions()
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync {
            map = it
            map?.mapType = GoogleMap.MAP_TYPE_SATELLITE
        }
        subscribeToObservers()
        setButtonOnClickListener()
        setInitialButtonVisibility()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        Log.d("track", "$isTracking")
        if (!isTracking) {
            Log.d("track", "reset chrono")
            binding.txtChrono.text == "00:00:00"
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    private fun setButtonOnClickListener() {
        binding.btnStart.setOnClickListener { startNewTrek() }
        binding.btnEndTrek.setOnClickListener { endTrek() }
    }

    private fun setInitialButtonVisibility() {
        binding.btnStart.visibility = View.VISIBLE
        binding.btnEndTrek.visibility = View.GONE
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (isTracking) {
            binding.btnStart.visibility = View.GONE
            binding.btnEndTrek.visibility = View.VISIBLE
        } else {
            binding.btnStart.visibility = View.VISIBLE
            binding.btnEndTrek.visibility = View.GONE
        }
    }

    private fun startNewTrek() {
        activity?.let {
            val locationManager = it.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                sendCommandToService(Constants.ACTION_START_SERVICE)
            } else {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.dialog_gps_disable_title))
                    .setMessage(getString(R.string.dialog_gps_disable_message))
                    .setNeutralButton(getString(R.string.dialog_gps_disable_btn_neutral_text)) { _, _ -> }
                    .show()
            }
        }
    }

    private fun endTrek() {
        viewModel.setCurrentTrekData(
            coordinates,
            timeInMs,
            getString(R.string.default_new_trek_name)
        )
        sendCommandToService(Constants.ACTION_STOP_SERVICE)
        val action = TrekFragmentDirections.actionTrekFragmentToTrekInfoFragment(0)
        findNavController().navigate(action)
    }

    private fun addPolyline() {
        polyLine?.let {
            it.remove() //remove the old polyline if it exist
            polyLine = null
        }
        if (coordinates.isNotEmpty()) {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .clickable(false)
            for (coordinate in coordinates) {
                polylineOptions.add(LatLng(coordinate.latitude, coordinate.longitude))
            }
            polyLine = map?.addPolyline(polylineOptions)
        }
    }

    private fun moveCameraToUser() {
        if (coordinates.isNotEmpty()) {
            val lastLoc = coordinates.last()
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(lastLoc.latitude, lastLoc.longitude),
                    MAP_CAMERA_ZOOM
                )
            )
        }
    }

    private fun updateTime() {
        val dateFormat = SimpleDateFormat("HH:mm:ss")
        dateFormat.timeZone = TimeZone.getTimeZone("GMT+0")
        binding.txtChrono.text = dateFormat.format(timeInMs)
    }

    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }

    @SuppressLint("MissingPermission")
    private fun subscribeToObservers() {
        TrackingService.coordinates.observe(viewLifecycleOwner) {
            coordinates = it
            addPolyline()
            moveCameraToUser()
            map?.isMyLocationEnabled = true
        }

        TrackingService.timeInMs.observe(viewLifecycleOwner) {
            timeInMs = it
            updateTime()
        }

        TrackingService.isTracking.observe(viewLifecycleOwner) {
            updateTracking(it)
        }
    }

    private fun checkPermissions() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED -> {
                //permission granted
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) -> {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.dialog_permission_location_title))
                    .setMessage(getString(R.string.dialog_permission_location_message))
                    .setNeutralButton(getString(R.string.dialog_permission_location_btn_neutral_text)) { _, _ ->
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                            )
                        )
                    }
                    .show()
            }
            else -> {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )
            }
        }
    }
}