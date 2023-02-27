package com.example.weather.bottomsheet

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.example.weather.Constants
import com.example.weather.R

object BottomSheetCreator {
    fun show(context: Context, text: String?) {
        val dialogFragment = NotificationBottomSheetDialog.newInstance()
        val bundle = Bundle()
        bundle.putString(Constants.BOTTOM_SHEET_TEXT, text)
        bundle.putString(Constants.BOTTOM_SHEET_BUTTON, context.getString(android.R.string.ok))
        dialogFragment.arguments = bundle
        val fragmentActivity: FragmentActivity = context as FragmentActivity
        dialogFragment.show(
            fragmentActivity.getSupportFragmentManager(),
            Constants.BOTTOM_SHEET_TAG
        )
    }
}