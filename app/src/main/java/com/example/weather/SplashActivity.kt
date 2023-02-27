package com.example.weather

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.weather.MainActivity

class SplashActivity : AppCompatActivity() {
    private var sharedPreferences: SharedPreferences? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        sharedPreferences = getSharedPreferences(Constants.MAIN_SHARED_NAME, MODE_PRIVATE)
        if (isChangedTheme) {
            return
        }
        startActivity(Intent(applicationContext, MainActivity::class.java))
        finish()
    }

    private val isChangedTheme: Boolean
        private get() {
            if (sharedPreferences!!.getBoolean(Constants.SHARED_THEME_IS_DARK, false) &&
                AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES
            ) {
                setTheme(true)
                return true
            } else if (!sharedPreferences!!.getBoolean(Constants.SHARED_THEME_IS_DARK, false) &&
                AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO
            ) {
                setTheme(false)
                return true
            }
            return false
        }

    fun setTheme(isDark: Boolean) {
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES
            )
        } else {
            AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
            )
        }
        startActivity(Intent(applicationContext, MainActivity::class.java))
        finish()
    }
}