package ch.kra.trek.ui.fragments

import android.content.Context
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ch.kra.trek.R
import ch.kra.trek.databinding.FragmentStartBinding
import ch.kra.trek.services.TrackingService
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class StartFragment : Fragment() {

    private var _binding: FragmentStartBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentStartBinding.inflate(inflater,  container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (TrackingService.isTracking.value == true) {
            binding.btnStartTrek.text = getString(R.string.continue_trek)
        }
        binding.btnStartTrek.setOnClickListener { startNewTrek() }
        binding.btnLoadTrek.setOnClickListener { loadTrek() }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun loadTrek() {
        val action = StartFragmentDirections.actionStartFragmentToLoadTrekFragment()
        findNavController().navigate(action)
    }


    private fun startNewTrek() {
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