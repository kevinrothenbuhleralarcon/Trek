package ch.kra.trek.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ch.kra.trek.R
import ch.kra.trek.TrekApplication
import ch.kra.trek.databinding.FragmentTrekBinding
import ch.kra.trek.other.Constants.ACTION_START_SERVICE
import ch.kra.trek.other.Constants.ACTION_STOP_SERVICE
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

class TrekFragment : Fragment() {

    private val startNewTrekRequestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (it[Manifest.permission.ACCESS_FINE_LOCATION] == true || it[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            startNewTrek()
        } else {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.dialog_permission_location_title))
                .setMessage(getString(R.string.dialog_permission_location_message))
                .setNeutralButton(getString(R.string.dialog_permission_location_btn_neutral_text)) { _, _ -> }
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
    private var pathPoints = mutableListOf<LatLng>()
    private var altitudes = mutableListOf<Double>()
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
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync {
            map = it
            map?.mapType = GoogleMap.MAP_TYPE_SATELLITE
            map?.isMyLocationEnabled = true
        }
        subscribeToObservers()
        setButtonOnClickListener()
        setInitialButtonVisibility()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun onResume() {
        super.onResume()
        Log.d("trek", "onResume")
        binding.mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        Log.d("trek", "onStart")
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        Log.d("trek", "onStop")
        binding.mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        Log.d("trek", "onPause")
        binding.mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Log.d("trek", "onLowMemory")
        binding.mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("trek", "onDestroy")
        binding.mapView.onDestroy()
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d("trek", "onSaveInstanceState")
        binding.mapView.onSaveInstanceState(outState)
    }

    private fun setButtonOnClickListener(){
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
        sendCommandToService(ACTION_START_SERVICE)
    }

    private fun endTrek() {
        sendCommandToService(ACTION_STOP_SERVICE)
        viewModel.setCurrentTrekData(pathPoints, altitudes, timeInMs, getString(R.string.default_new_trek_name))
        val action = TrekFragmentDirections.actionTrekFragmentToTrekInfoFragment(0)
        findNavController().navigate(action)
    }

    private fun addPolyline() {
        polyLine?.let {
            it.remove() //remove the old polyline if it exist
            polyLine = null
        }
        if (pathPoints.isNotEmpty()) {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .clickable(false)
                .addAll(pathPoints)
            polyLine = map?.addPolyline(polylineOptions)
        }
    }

    private fun moveCameraToUser() {
        if (pathPoints.isNotEmpty()) {
            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(pathPoints.last(), MAP_CAMERA_ZOOM))
        }
    }

    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }

    private fun subscribeToObservers() {
        TrackingService.pathPoint.observe(viewLifecycleOwner) {
            pathPoints = it
            addPolyline()
            moveCameraToUser()
        }

        TrackingService.altitudes.observe(viewLifecycleOwner) {
            altitudes = it
        }

        TrackingService.timeInMs.observe(viewLifecycleOwner) {
            timeInMs = it
        }

        TrackingService.isTracking.observe(viewLifecycleOwner) {
            updateTracking(it)
        }
    }
}