package ch.kra.trek.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import ch.kra.trek.TrekApplication
import ch.kra.trek.databinding.FragmentLoadTrekBinding
import ch.kra.trek.repositories.TrekRepository
import ch.kra.trek.ui.adapter.LoadTrekAdapter
import ch.kra.trek.ui.viewmodels.TrekViewModel

class LoadTrekFragment : Fragment() {

    private var _binding: FragmentLoadTrekBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TrekViewModel by activityViewModels {
        TrekViewModel.TrekViewModelFactory(TrekRepository((activity?.application as TrekApplication).database.trekDao()))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoadTrekBinding.inflate(inflater, container, false)
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



    private fun displayTrek(trekId: Int){
        val action = LoadTrekFragmentDirections.actionLoadTrekFragmentToTrekInfoFragment(trekId)
        findNavController().navigate(action)
    }
}