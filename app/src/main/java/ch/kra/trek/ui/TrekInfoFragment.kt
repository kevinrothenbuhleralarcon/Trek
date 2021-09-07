package ch.kra.trek.ui

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import ch.kra.trek.R
import ch.kra.trek.database.TrekApplication
import ch.kra.trek.databinding.FragmentTrekInfoBinding
import ch.kra.trek.helper.Trek
import ch.kra.trek.viewmodel.TrekViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class TrekInfoFragment : Fragment(), OnMapReadyCallback {

    private val navigationArgs: TrekInfoFragmentArgs by navArgs()

    private var _binding: FragmentTrekInfoBinding? = null
    private val binding get() = _binding!!

    private lateinit var mMap: GoogleMap

    private val viewModel: TrekViewModel by activityViewModels {
        TrekViewModel.TrekViewModelFactory((activity?.application as TrekApplication).database.trekDao())
    }

    private lateinit var trek: Trek

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_trek_info, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_detail) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        if (navigationArgs.trekId == 0) { //in this case it's a new trek and we need to provide the ability to save it
            binding.btnSaveDeleteTrek.text = getString(R.string.btn_save_trek_text)
            binding.btnSaveDeleteTrek.setOnClickListener { saveTrek() }
        } else { // In this case the trek is a loaded one and we need to provide the ability to delete it
            binding.btnSaveDeleteTrek.text = getString(R.string.btn_delete_trek_text)
            binding.btnSaveDeleteTrek.setOnClickListener { deleteTrek() }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val trekId = navigationArgs.trekId
        viewModel.getTrek(trekId).observe(viewLifecycleOwner) { selectedTrek ->
            trek = selectedTrek
            bindData(selectedTrek)
        }
    }

    private fun saveTrek() {
        val editText = EditText(requireContext())
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.dialog_save_title))
            .setMessage(getString(R.string.dialog_save_message))
            .setCancelable(false)
            .setView(editText)
            .setPositiveButton(getString(R.string.dialog_save_btn_positive_text)) { _, _ ->}
            .setNegativeButton(getString(R.string.dialog_save_btn_negative_text)) { _, _ ->}
            .show()
        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if (editText.text.toString() != "") {
                trek.trekName = editText.text.toString()
                viewModel.saveTrek(trek)
                binding.btnSaveDeleteTrek.visibility = View.GONE
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), getString(R.string.toast_save), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun deleteTrek(){
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.dialog_delete_title))
            .setMessage(getString(R.string.dialog_delete_message))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.dialog_delete_btn_positive_text)) { _, _ ->
                viewModel.deleteTrek(trek)
                val action = TrekInfoFragmentDirections.actionTrekInfoFragmentToLoadTrekFragment()
                findNavController().navigate(action)
            }
            .setNegativeButton(getString(R.string.dialog_delete_btn_negative_text)) { _, _ -> }
            .show()
    }

    private fun bindData(trek: Trek) {
        Log.d("trek", "bindData called")
        val dateFormat = SimpleDateFormat("HH:mm:ss")
        dateFormat.timeZone = TimeZone.getTimeZone("GMT+0")

        val decimalFormat = DecimalFormat("0.00")

        binding.lblTime.text =dateFormat.format(Date(trek.time))
        binding.lblKm.text = "${decimalFormat.format(trek.km / 1000)} km"
        binding.lblMaxDrop.text = "${decimalFormat.format(trek.maxDrop)} m"
        binding.lblTotalDrop.text = "${decimalFormat.format(trek.totalDrop)} m"
        displayRoad(trek.listLatLng)
    }

    private fun displayRoad(road: List<LatLng>) {
        if (road.isNotEmpty()) {
            val polylineOption = PolylineOptions()
            polylineOption.addAll(road)
            /*for (coordinate in road) {
            polylineOption.add(coordinate)
            }*/
            mMap.addPolyline(polylineOption)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(polylineOption.points.first()))
            mMap.moveCamera(CameraUpdateFactory.zoomTo(13f))
        }
    }
}