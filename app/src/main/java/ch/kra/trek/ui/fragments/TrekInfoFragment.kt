package ch.kra.trek.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import ch.kra.trek.R
import ch.kra.trek.TrekApplication
import ch.kra.trek.database.Coordinate
import ch.kra.trek.database.TrekData
import ch.kra.trek.databinding.FragmentTrekInfoBinding
import ch.kra.trek.other.Constants.MAP_CAMERA_ZOOM
import ch.kra.trek.other.Constants.POLYLINE_COLOR
import ch.kra.trek.other.Constants.POLYLINE_WIDTH
import ch.kra.trek.ui.viewmodels.TrekViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class TrekInfoFragment : Fragment() {

    private val navigationArgs: TrekInfoFragmentArgs by navArgs()

    private var _binding: FragmentTrekInfoBinding? = null
    private val binding get() = _binding!!

    private var map: GoogleMap? = null

    private val viewModel: TrekViewModel by activityViewModels {
        TrekViewModel.TrekViewModelFactory((activity?.application as TrekApplication).database.trekDao())
    }

    private var trekId: Int = 0
    private var trek: TrekData? = null
    private var isNewUnsavedTrek: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true) // in order to be able to catch the navigate up button

        //what to do if the user try to navigate back
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (isNewUnsavedTrek) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.dialog_info_back_title))
                    .setMessage(getString(R.string.dialog_info_back_message))
                    .setPositiveButton(getString(R.string.dialog_info_back_btn_positive_text)) { _, _ ->
                        this.remove() //remove this callback as we need to perform the backPressed action
                        val action = TrekInfoFragmentDirections.actionTrekInfoFragmentToStartFragment()
                        findNavController().navigate(action)
                    }
                    .setNegativeButton(R.string.dialog_info_back_btn_negative_text) { _, _ -> }
                    .show()
            } else {
                this.remove()
                if (trekId == 0) {
                    val action = TrekInfoFragmentDirections.actionTrekInfoFragmentToStartFragment()
                    findNavController().navigate(action)
                } else {
                    requireActivity().onBackPressed()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_trek_info, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        trekId = navigationArgs.trekId

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync {
            map = it
            map?.mapType = GoogleMap.MAP_TYPE_SATELLITE
            subscribeToObserver()
        }

        if (trekId == 0) { //in this case it's a new trek and we need to provide the ability to save it
            binding.btnSaveDeleteTrek.text = getString(R.string.btn_save_trek_text)
            binding.btnSaveDeleteTrek.setOnClickListener { saveTrek() }
            isNewUnsavedTrek = true
        } else { // In this case the trek is a loaded one and we need to provide the ability to delete it
            binding.btnSaveDeleteTrek.text = getString(R.string.btn_delete_trek_text)
            binding.btnSaveDeleteTrek.setOnClickListener { deleteTrek() }
            isNewUnsavedTrek = false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            requireActivity().onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
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

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
        _binding = null
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
                trek?.let { trek ->
                    trek.trekName = editText.text.toString()
                    viewModel.saveTrek(trek)
                    binding.btnSaveDeleteTrek.visibility = View.GONE
                    isNewUnsavedTrek = false
                    dialog.dismiss()
                    changeTitle()
                }

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
                trek?.let {
                    viewModel.deleteTrek(it)
                }
                val action = TrekInfoFragmentDirections.actionTrekInfoFragmentToLoadTrekFragment()
                findNavController().navigate(action)
            }
            .setNegativeButton(getString(R.string.dialog_delete_btn_negative_text)) { _, _ -> }
            .show()
    }

    private fun bindData(trek: TrekData) {
        val dateFormat = SimpleDateFormat("HH:mm:ss")
        dateFormat.timeZone = TimeZone.getTimeZone("GMT+0") //needed on physical device for a chrono in order that it start at 0
        val decimalFormat = DecimalFormat("0.00")

        binding.lblTime.text =dateFormat.format(Date(trek.time))
        binding.lblKm.text = "${decimalFormat.format(trek.km / 1000)} km"
        binding.lblPositiveNegativeDrop.text = "${decimalFormat.format(trek.totalPositiveDrop)} m / ${decimalFormat.format(trek.totalNegativeDrop)} m"
        binding.lblTotalDrop.text = "${decimalFormat.format(trek.totalPositiveDrop + trek.totalNegativeDrop)} m"
        displayRoad(trek.coordinates)
    }

    private fun displayRoad(road: List<Coordinate>) {
        if (road.isNotEmpty()) {
            val polylineOption = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .clickable(false)
            for (coordinate in road)
            {
                polylineOption.add(LatLng(coordinate.latitude, coordinate.longitude))
            }
            map?.addPolyline(polylineOption)
            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(road.first().latitude, road.first().longitude), MAP_CAMERA_ZOOM))
        }
    }

    private fun subscribeToObserver() {
        viewModel.getTrek(trekId).observe(viewLifecycleOwner) { selectedTrek ->
            trek = selectedTrek
            bindData(selectedTrek)
            changeTitle()
        }
    }

    private fun changeTitle()
    {
        val title = trek?.let { it.trekName } ?: getString(R.string.default_new_trek_name)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = title
    }
}