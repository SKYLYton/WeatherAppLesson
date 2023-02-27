package com.example.weather.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.weather.Constants
import com.example.weather.R
import com.example.weather.databinding.BottomDialogNotificationBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class NotificationBottomSheetDialog : BottomSheetDialogFragment() {

    private var _binding: BottomDialogNotificationBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomDialogNotificationBinding.inflate(inflater, container, false)
        isCancelable = true
        arguments?.let {
            binding.textViewText.text = it.getString(Constants.BOTTOM_SHEET_TEXT)
            binding.textViewButton.text = it.getString(Constants.BOTTOM_SHEET_BUTTON)
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): NotificationBottomSheetDialog {
            return NotificationBottomSheetDialog()
        }
    }
}