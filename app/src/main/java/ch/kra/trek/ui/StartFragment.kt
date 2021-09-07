package ch.kra.trek.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ch.kra.trek.R
import ch.kra.trek.databinding.FragmentStartBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class StartFragment : Fragment() {

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
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

    private var _binding: FragmentStartBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_start, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.startFragment = this
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    fun loadTrek() {
        val action = StartFragmentDirections.actionStartFragmentToLoadTrekFragment()
        findNavController().navigate(action)
    }

    fun startNewTrek() {
        //before we can start a new trek we have to check the permission
        if (isLocationPermissionGranted()) {
            val locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                val action = StartFragmentDirections.actionStartFragmentToTrekFragment()
                findNavController().navigate(action)
            } else {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.dialog_gps_disable_title))
                    .setMessage(getString(R.string.dialog_gps_disable_message))
                    .setNeutralButton(getString(R.string.dialog_gps_disable_btn_neutral_text)) { _,_ -> }
                    .show()
            }
        }
    }

    private fun isLocationPermissionGranted(): Boolean {
        return when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED-> {
                //Action if the permission is already granted
                true
            }

            ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)-> {
                //Action if the app need think it need to show the request permission radial
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.dialog_permission_location_title))
                    .setMessage(getString(R.string.dialog_permission_location_message))
                    .setNeutralButton(getString(R.string.dialog_permission_location_btn_neutral_text)) { _, _ ->
                        requestPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        )
                    }
                    .show()
                false
            }

            else -> {
                //action if the permission hasn't been asked yet
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )
                false
            }
        }
    }
}