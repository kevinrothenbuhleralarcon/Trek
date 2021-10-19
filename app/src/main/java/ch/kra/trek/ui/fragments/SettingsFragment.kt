package ch.kra.trek.ui.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ch.kra.trek.databinding.FragmentSettingsBinding
import ch.kra.trek.other.Constants
import ch.kra.trek.other.Constants.MAP_TYPE

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = requireActivity().getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        setRadioBasedOnPref()
        binding.btnSaveOptions.setOnClickListener { savePreferences() }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setRadioBasedOnPref() {
        binding.radioGroupMapType.check(sharedPreferences.getInt(MAP_TYPE, -1))
    }

    private fun savePreferences() {
        val rBtn = binding.radioGroupMapType.checkedRadioButtonId
        sharedPreferences.edit().putInt(MAP_TYPE, rBtn).apply()
        requireActivity().onBackPressed()
    }
}