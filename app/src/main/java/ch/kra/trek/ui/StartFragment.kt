package ch.kra.trek.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ch.kra.trek.R
import ch.kra.trek.databinding.FragmentStartBinding

class StartFragment : Fragment() {

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

    fun startNewTrek() {
        val action = StartFragmentDirections.actionStartFragmentToTrekFragment()
        findNavController().navigate(action)
    }

    fun loadTrek() {
        val action = StartFragmentDirections.actionStartFragmentToLoadTrekFragment()
        findNavController().navigate(action)
    }
}