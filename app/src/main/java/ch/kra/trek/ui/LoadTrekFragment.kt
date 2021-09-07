package ch.kra.trek.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import ch.kra.trek.R
import ch.kra.trek.adapter.LoadTrekAdapter
import ch.kra.trek.database.TrekApplication
import ch.kra.trek.databinding.FragmentLoadTrekBinding
import ch.kra.trek.viewmodel.TrekViewModel

class LoadTrekFragment : Fragment() {

    private var _binding: FragmentLoadTrekBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TrekViewModel by activityViewModels {
        TrekViewModel.TrekViewModelFactory((activity?.application as TrekApplication).database.trekDao())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_load_trek, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = LoadTrekAdapter(this::displayTrek)
        binding.recyclerTrekList.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerTrekList.adapter = adapter
        viewModel.trekDataList.observe(this.viewLifecycleOwner) { listTrek ->
            listTrek.let {
                adapter.submitList(it)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    fun displayTrek(trekId: Int){
        val action = LoadTrekFragmentDirections.actionLoadTrekFragmentToTrekInfoFragment(trekId)
        findNavController().navigate(action)
    }
}