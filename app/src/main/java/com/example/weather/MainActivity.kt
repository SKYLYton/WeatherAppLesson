package com.example.weather

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import android.os.SystemClock
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.weather.databinding.ActivityMainBinding
import com.example.weather.databinding.ActivitySideMenuBinding
import com.example.weather.fragments.AboutFragment
import com.example.weather.fragments.CountryFragment
import com.example.weather.fragments.SettingsFragment
import com.example.weather.receivers.NetworkReceiver
import com.example.weather.receivers.PowerConnectionReceiver
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var bindingSide: ActivitySideMenuBinding

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var editor: SharedPreferences.Editor
    private var currentMainParcel: MainParcel? = null


    private lateinit var textViewCountry: TextView

    private var networkReceiver: NetworkReceiver? = null
    private var powerConnectionReceiver: PowerConnectionReceiver? = null
    private var mLastClickTime: Long = 0
    var currentTab = TAB_MAIN
    private var frgManager: FragmentManager? = null
    private lateinit var mStacks: MutableMap<String, Stack<Fragment>>

    var isSideMenu = false
        private set
    private var currentCountryText: String = ""
    lateinit var selectedLocation: SelectedLocation
        private set

    fun setCountryText(countryText: String) {
        currentMainParcel?.currentCountry = countryText
        if (isSideMenu) {
            textViewCountry.text = countryText
        }
    }

    protected override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(Constants.CURRENT_PARCEL, currentMainParcel)
        super.onSaveInstanceState(outState)
    }

    @SuppressLint("CommitPrefEdits")
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences(Constants.MAIN_SHARED_NAME, Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
        loadSavedData()
        if (!isSideMenu) {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
        } else {
            bindingSide = ActivitySideMenuBinding.inflate(layoutInflater)
            setContentView(bindingSide.root)
        }

        if (savedInstanceState != null) {
            currentMainParcel =
                savedInstanceState.getParcelable(Constants.CURRENT_PARCEL)
            currentTab = currentMainParcel!!.currentTab
            currentCountryText = currentMainParcel!!.currentCountry
        } else {
            currentMainParcel = MainParcel(false, TAB_MAIN, currentCountryText)
        }
        frgManager = supportFragmentManager
        initStack()
        if (!isSideMenu) {
            initBottomNavView()
        } else {
            val toolbar = initToolbar()
            initNavView(toolbar)
        }
        initBroadcastReceiverNetwork()
        initBroadcastReceiverPower()
    }

    private fun loadSavedData() {
        val isCord: Boolean = sharedPreferences.getBoolean(Constants.SHARED_TYPE_CORD, false)
        val nameCity: String? = sharedPreferences.getString(
            Constants.SHARED_COUNTRY_NAME,
            resources.getStringArray(R.array.cities)[0]
        )
        selectedLocation = if (isCord) {
            val lat: Double =
                sharedPreferences.getFloat(Constants.SHARED_LAT, 0f).toDouble()
            val lng: Double =
                sharedPreferences.getFloat(Constants.SHARED_LNG, 0f).toDouble()
            SelectedLocation(lat, lng, isCord, nameCity)
        } else {
            val currentCityId: Int = sharedPreferences.getInt(
                Constants.SHARED_COUNTRY_ID,
                Constants.DEFAULT_CITY_ID
            )
            SelectedLocation(nameCity, currentCityId)
        }
        isSideMenu = sharedPreferences.getString(
            Constants.SHARED_MENU_TYPE,
            Constants.SHARED_SIDE_MENU
        ) == Constants.SHARED_SIDE_MENU
    }

    private fun initBroadcastReceiverNetwork() {
        val imageViewNetwork: ImageView = findViewById<ImageView>(R.id.imageViewNetwork)
        val intentFilter = IntentFilter()
        intentFilter.addAction(Constants.CONNECTIVITY_ACTION)
        networkReceiver = NetworkReceiver()
        networkReceiver?.let {
            registerReceiver(it, intentFilter)
            it.onNetworkStateListener = { isConnected ->
                imageViewNetwork.setImageResource(
                    if (isConnected) R.drawable.ic_signal else R.drawable.ic_no_signal
                )
            }
        }
    }

    private fun initBroadcastReceiverPower() {
        val imageViewBattery: ImageView = findViewById<ImageView>(R.id.imageViewBattery)
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        powerConnectionReceiver = PowerConnectionReceiver()
        powerConnectionReceiver?.let {
            registerReceiver(it, intentFilter)
            it.setOnPowerStateListener(object :
                PowerConnectionReceiver.OnPowerStateListener {
                override fun onCharging() {
                    imageViewBattery.setImageResource(R.drawable.ic_battery_charging)
                }

                override fun onNormalLevel() {
                    imageViewBattery.setImageResource(R.drawable.ic_battery_full)
                }

                override fun onLowLevel() {
                    imageViewBattery.setImageResource(R.drawable.ic_battery_alert)
                }

                override fun onCriticalLowLevel() {
                    imageViewBattery.setImageResource(R.drawable.ic_battery_low)
                }
            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        val settings = menu.findItem(R.id.action_settings)
        settings.setOnMenuItemClickListener {
            if (isSideMenu) {
                currentTab = TAB_SETTINGS
                setSelectedItemNavView(TAB_SETTINGS_ID)
                selectedTab()
            }
            true
        }
        return true
    }

    private fun initStack() {
        mStacks = HashMap()
        mStacks[TAB_MAIN] = Stack()
        mStacks[TAB_ABOUT] = Stack()
        mStacks[TAB_SETTINGS] = Stack()
    }

    private fun initToolbar(): Toolbar {
        if (isSideMenu) {
            setSupportActionBar(bindingSide.appBarMain.toolbar)
        }
        return bindingSide.appBarMain.toolbar
    }

    private fun initNavView(toolbar: Toolbar) {
        val view: View = bindingSide.navView.getHeaderView(0)
        textViewCountry = view.findViewById(R.id.textViewCountry)
        textViewCountry.text = currentMainParcel!!.currentCountry
        val toggle = ActionBarDrawerToggle(
            this, bindingSide.drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        bindingSide.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        bindingSide.navView.setNavigationItemSelectedListener(object :
            NavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                if (selectedItem(item)) {
                    supportActionBar?.setTitle(item.title)
                    bindingSide.drawerLayout.closeDrawer(GravityCompat.START)
                    return true
                }
                return false
            }
        })
        var id = 0
        when (currentTab) {
            TAB_MAIN -> id = TAB_MAIN_ID
            TAB_ABOUT -> id = TAB_ABOUT_ID
            TAB_SETTINGS -> id = TAB_SETTINGS_ID
        }
        setSelectedItemNavView(id)
        selectedTab()
    }

    private fun setSelectedItemNavView(id: Int) {
        supportActionBar?.setTitle(bindingSide.navView.menu.getItem(id).title)
        bindingSide.navView.setCheckedItem(bindingSide.navView.menu.getItem(id))
    }

    private fun initBottomNavView() {
        binding.bottomNavView.setOnNavigationItemSelectedListener { item -> selectedItem(item) }
        var id = 0
        when (currentTab) {
            TAB_MAIN -> id = R.id.navigation_home
            TAB_ABOUT -> id = R.id.navigation_about
            TAB_SETTINGS -> id = R.id.navigation_settings
        }
        binding.bottomNavView.selectedItemId = id
        binding.bottomNavView.setOnNavigationItemReselectedListener(object :
            BottomNavigationView.OnNavigationItemReselectedListener {
            override fun onNavigationItemReselected(item: MenuItem) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
                    return
                }
                mLastClickTime = SystemClock.elapsedRealtime()
                if (mStacks[currentTab]!!.size > 1) {
                    mStacks[currentTab]!!.subList(1, mStacks[currentTab]!!.size).clear()
                    selectedTab()
                }
            }
        })
    }

    @SuppressLint("NonConstantResourceId")
    private fun selectedItem(item: MenuItem): Boolean {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
            return false
        }
        mLastClickTime = SystemClock.elapsedRealtime()
        when (item.itemId) {
            R.id.navigation_home -> currentTab = TAB_MAIN
            R.id.navigation_about -> currentTab = TAB_ABOUT
            R.id.navigation_settings -> currentTab = TAB_SETTINGS
        }
        currentMainParcel!!.currentTab = currentTab
        selectedTab()
        return true
    }

    private fun selectedTab() {
        if (mStacks[currentTab]!!.empty()) {
            var fragment: Fragment? = null
            when (currentTab) {
                TAB_MAIN -> fragment = CountryFragment()
                TAB_ABOUT -> fragment = AboutFragment()
                TAB_SETTINGS -> fragment = SettingsFragment()
            }
            pushFragments(fragment, true, null)
        } else {
            pushFragments(mStacks[currentTab]!!.lastElement(), false, null)
        }
    }

    fun pushFragments(fragment: Fragment?, addToBS: Boolean, bundle: Bundle?) {
        if (addToBS) {
            mStacks[currentTab]!!.push(fragment)
        }
        if (bundle != null) {
            fragment!!.arguments = bundle
        }
        val ft = frgManager!!.beginTransaction()
        if (!isSideMenu) {
            ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
        }
        ft.replace(R.id.container, fragment!!)
        ft.commit()
    }

    fun changeCountry(id: Int, country: String?) {
        selectedLocation.cityName = country
        selectedLocation.cityId = id
        selectedLocation.lat = 0.0
        selectedLocation.lng = 0.0
        selectedLocation.isCord = false
    }

    fun changeCountry(lat: Double, lng: Double) {
        selectedLocation.cityName = ""
        selectedLocation.cityId = 0
        selectedLocation.lat = lat
        selectedLocation.lng = lng
        selectedLocation.isCord = true
    }

    fun popFragments() {
        mStacks[currentTab]!!.pop()
        val fragment = mStacks[currentTab]!!.elementAt(
            mStacks[currentTab]!!.size - 1
        )
        pushFragments(fragment, false, null)
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
        editor.putBoolean(Constants.SHARED_THEME_IS_DARK, isDark)
        editor.commit()
    }

    fun setTypeMenu(isSide: Boolean) {
        editor.putString(
            Constants.SHARED_MENU_TYPE,
            if (isSide) Constants.SHARED_SIDE_MENU else Constants.SHARED_BOTTOM_MENU
        )
        editor.commit()
        recreate()
    }

    val isDarkTheme: Boolean
        get() = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES

    override fun onBackPressed() {
        if (isSideMenu && bindingSide.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            bindingSide.drawerLayout.closeDrawer(GravityCompat.START)
            return
        }
        if (mStacks[currentTab]!!.size <= 1) {
            super.onBackPressed()
            return
        }
        popFragments()
    }

    protected override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(networkReceiver)
        unregisterReceiver(powerConnectionReceiver)
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val TAB_MAIN = "tab_main"
        private const val TAB_ABOUT = "tab_about"
        private const val TAB_SETTINGS = "tab_settings"
        private const val TAB_MAIN_ID = 0
        private const val TAB_ABOUT_ID = 1
        private const val TAB_SETTINGS_ID = 2
    }
}