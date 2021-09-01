package ch.kra.trek.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ch.kra.trek.R
import ch.kra.trek.database.TrekApplication
import ch.kra.trek.databinding.FragmentTrekBinding
import ch.kra.trek.viewmodel.TrekViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions

class TrekFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    var _binding: FragmentTrekBinding? = null
    val binding get() = _binding!!

    val viewModel: TrekViewModel by activityViewModels {
        TrekViewModel.TrekViewModelFactory((activity?.application as TrekApplication).database.trekDao())
    }

    private val polylineOption = PolylineOptions().clickable(false)

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
        viewModel.trekCoordinates.observe(viewLifecycleOwner) {
            Log.d("line", "Change observed")
            if (it.isNotEmpty()){
                Log.d("line", "not empty")
                polylineOption.add(it.last())
                mMap.addPolyline(polylineOption)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)

        mMap.addPolyline(polylineOption)

        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    fun startNewTrek()
    {
        viewModel.setTrekToLoad(0)
        viewModel.startTrek()
        binding.btnStartPause.text = getString(R.string.btn_start_pause_pause_text)
    }
}