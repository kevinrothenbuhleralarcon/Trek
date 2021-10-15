package ch.kra.trek.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import ch.kra.trek.R
import ch.kra.trek.databinding.FragmentSettingsBinding
import ch.kra.trek.ui.MainActivity

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).changeTitle(getString(R.string.settings_fragment_title))
        val spinnerAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.language_array, android.R.layout.simple_spinner_item).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerLanguage.adapter = spinnerAdapter
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}