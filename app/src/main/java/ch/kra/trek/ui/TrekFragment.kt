package ch.kra.trek.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ch.kra.trek.R
import ch.kra.trek.database.TrekApplication
import ch.kra.trek.databinding.FragmentTrekBinding
import ch.kra.trek.viewmodel.TrekViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class TrekFragment : Fragment(), OnMapReadyCallback {

    private val setMapRequestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (it[Manifest.permission.ACCESS_FINE_LOCATION] == true || it[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            setMapDefaultLocation()
        } else {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.dialog_permission_location_title))
                .setMessage(getString(R.string.dialog_permission_location_message))
                .setNeutralButton(getString(R.string.dialog_permission_location_btn_neutral_text)) { _, _ -> }
                .show()
        }
    }

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

    private lateinit var locationManager: LocationManager
    private lateinit var lineRoute: Polyline
    private lateinit var mMap: GoogleMap
    private var isLocationRequestEnabled = false
    private var _binding: FragmentTrekBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true) // in order to be able to catch the navigate up button

        //what to do if the user try to navigate back
        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.dialog_back_title))
                .setMessage(getString(R.string.dialog_back_message))
                .setPositiveButton(getString(R.string.dialog_back_btn_positive_text)) { _, _ ->
                    this.remove() //remove this callback as we need to perform the backPressed action
                    requireActivity().onBackPressed() //call the back pressed action again
                }
                .setNegativeButton(R.string.dialog_back_btn_negative_text) { _, _ -> }
                .show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            requireActivity().onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_trek, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        binding.btnStartPause.text = getString(R.string.btn_start_pause_start_text)
        binding.trekFragment = this
        locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        viewModel.trekCoordinates.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                lineRoute.points = it //add the new list of points
                mMap.moveCamera(CameraUpdateFactory.newLatLng(it.last())) //center the camera to the last coordinate
            }
        }
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun onPause() {
        super.onPause()
        /*if (isLocationRequestEnabled &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(viewModel)
        }*/
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val polylineOption = PolylineOptions().clickable(false)
        lineRoute = mMap.addPolyline(polylineOption)
        setMapDefaultLocation()
    }

    @SuppressLint("MissingPermission") //Warning suppressed because permission is set in the Manifest and for some reason is still ask for it to be added in the Manifest
    fun startNewTrek() {
        //check if the permission is already given
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //Action if the permission is already granted
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) { //check if the GPS is active
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0f, viewModel) //start receiving new location update, trigger is on the viewModel

                viewModel.startTrek()
                binding.btnStartPause.isEnabled = false
            } else {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.dialog_gps_disable_title))
                    .setMessage(getString(R.string.dialog_gps_disable_message))
                    .setNeutralButton(getString(R.string.dialog_gps_disable_btn_neutral_text)) { _, _ -> }
                    .show()
            }
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) ||
            ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            //Action if the app need think it need to show the request permission radial
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.dialog_permission_location_title))
                .setMessage(getString(R.string.dialog_permission_location_message))
                .setNeutralButton(getString(R.string.dialog_permission_location_btn_neutral_text)) { _, _ ->
                    startNewTrekRequestPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    )
                }
                .show()
        } else {
            //action if the permission hasn't been asked yet
            startNewTrekRequestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    fun endTrek() {
        locationManager.removeUpdates(viewModel) //stop getting new location
        viewModel.endTrek()
        val action = TrekFragmentDirections.actionTrekFragmentToTrekInfoFragment(0)
        findNavController().navigate(action)
    }

    @SuppressLint("MissingPermission") //Warning suppressed because permission is set in the Manifest and for some reason is still ask for it to be added in the Manifest
    private fun setMapDefaultLocation() {
        val locationToDisplay: LatLng
        val lausanne = LatLng(46.516, 6.63282)
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //Action if the permission is already granted
            locationToDisplay = if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) { //Check if the GPS is active
                val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (lastKnownLocation != null) {
                    LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude)
                } else {
                    lausanne
                }
            } else {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.dialog_gps_disable_title))
                    .setMessage(getString(R.string.dialog_gps_disable_message))
                    .setNeutralButton(getString(R.string.dialog_gps_disable_btn_neutral_text)) { _, _ -> }
                    .show()
                lausanne
            }
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) ||
            ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            //Action if the app need think it need to show the request permission radial
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.dialog_permission_location_title))
                .setMessage(getString(R.string.dialog_permission_location_message))
                .setNeutralButton(getString(R.string.dialog_permission_location_btn_neutral_text)) { _, _ ->
                    setMapRequestPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    )
                }
                .show()
            locationToDisplay = lausanne
        } else {
            //action if the permission hasn't been asked yet
            setMapRequestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
            locationToDisplay = lausanne
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(lausanne))
        mMap.moveCamera(CameraUpdateFactory.zoomTo(15f))
        mMap.isMyLocationEnabled = true
    }
}