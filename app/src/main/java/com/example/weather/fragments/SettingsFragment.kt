package com.example.weather.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import com.example.weather.Constants
import com.example.weather.MainActivity
import com.example.weather.R
import com.example.weather.databinding.FragmentSearchBinding
import com.example.weather.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding
        get() = _binding!!

    private lateinit var mainActivity: MainActivity
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        mainActivity = activity as MainActivity
        sharedPreferences =
            requireContext().getSharedPreferences(Constants.MAIN_SHARED_NAME, Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
        init()
        return binding.root
    }

    private fun init() {
        binding.radioButtonDark.isChecked = mainActivity.isDarkTheme
        binding.radioGroupTheme.setOnCheckedChangeListener { _, i ->
            when (i) {
                R.id.radioButtonDark -> mainActivity.setTheme(true)
                R.id.radioButtonLight -> mainActivity.setTheme(false)
            }
        }
        binding.radioButtonBottom.isChecked = !mainActivity.isSideMenu
        binding.radioGroupTypeMenu.setOnCheckedChangeListener { _, i ->
            when (i) {
                R.id.radioButtonSide -> mainActivity.setTypeMenu(true)
                R.id.radioButtonBottom -> mainActivity.setTypeMenu(false)
            }
        }
    }
}