package com.example.weather.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager

class PowerConnectionReceiver : BroadcastReceiver() {
    enum class Type {
        empty, charging, normal, low, critical
    }

    private val LOW_LEVEL_BATTERY_FOR_SENDING = 40
    private val CRITICAL_LOW_LEVEL_BATTERY_FOR_SENDING = 10
    private var currentType = Type.empty
    private var onPowerStateListener: OnPowerStateListener? = null

    interface OnPowerStateListener {
        fun onCharging()
        fun onNormalLevel()
        fun onLowLevel()
        fun onCriticalLowLevel()
    }

    fun setOnPowerStateListener(onPowerStateListener: OnPowerStateListener?) {
        this.onPowerStateListener = onPowerStateListener
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (onPowerStateListener == null) {
            return
        }
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
            if (currentType != Type.charging) {
                currentType = Type.charging
                onPowerStateListener!!.onCharging()
            }
        } else if (level > LOW_LEVEL_BATTERY_FOR_SENDING) {
            if (currentType != Type.normal) {
                currentType = Type.normal
                onPowerStateListener!!.onNormalLevel()
            }
        } else if (level > CRITICAL_LOW_LEVEL_BATTERY_FOR_SENDING) {
            if (currentType != Type.low) {
                currentType = Type.low
                onPowerStateListener!!.onLowLevel()
            }
        } else {
            if (currentType != Type.critical) {
                currentType = Type.critical
                onPowerStateListener!!.onCriticalLowLevel()
            }
        }
    }
}