package ch.kra.trek.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ch.kra.trek.R
import ch.kra.trek.database.TrekApplication
import ch.kra.trek.database.TrekWithCoordinates
import ch.kra.trek.databinding.FragmentTrekInfoBinding
import ch.kra.trek.viewmodel.TrekViewModel

class TrekInfoFragment : Fragment() {

    private var _binding: FragmentTrekInfoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TrekViewModel by activityViewModels {
        TrekViewModel.TrekViewModelFactory((activity?.application as TrekApplication).database.trekDao())
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
        binding.viewModel = viewModel
        binding.trekInfoFragment = this
        viewModel.trekWithCoordinates.observe(viewLifecycleOwner) { trek ->
            trek.let {
                bind(it) //Need to bind manually because if the trek is not finished loading before the fragment is loaded the trek info is not displayed
            }
        }
    }

    private fun bind(trekWithCoordinates: TrekWithCoordinates) {
        with(trekWithCoordinates.trek){
            binding.lblKm.text = km.toString()
            binding.lblTime.text = time.toString()
            binding.lblMaxDrop.text = maxDrop.toString()
            binding.lblTotalDrop.text = totalDrop.toString()
        }
        binding.lblCoordinates.text = trekWithCoordinates.coordinates.toString()

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    fun saveTrek() {
        viewModel.saveTrek()
    }

    fun deleteTrek(){
        viewModel.deleteTrek()
        val action = TrekInfoFragmentDirections.actionTrekInfoFragmentToLoadTrekFragment()
        findNavController().navigate(action)
    }
}