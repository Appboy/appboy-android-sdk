package com.appboy.sample.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.preference.PreferenceManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.appboy.enums.CardCategory
import com.appboy.sample.FeedCategoriesFragment
import com.appboy.sample.FeedCategoriesFragment.NoticeDialogListener
import com.appboy.sample.InAppMessageTesterFragment
import com.appboy.sample.MainFragment
import com.appboy.sample.PushTesterFragment
import com.appboy.sample.R
import com.appboy.sample.activity.settings.SettingsFragment
import com.appboy.sample.util.RuntimePermissionUtils.requestLocationPermissions
import com.appboy.sample.util.ViewUtils
import com.appboy.ui.AppboyFeedFragment
import com.braze.Braze
import com.braze.Constants
import com.braze.configuration.BrazeConfigurationProvider
import com.braze.support.BrazeLogger.Priority.E
import com.braze.support.BrazeLogger.Priority.I
import com.braze.support.BrazeLogger.brazelog
import com.braze.support.hasPermission
import com.braze.ui.contentcards.ContentCardsFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.util.*

class DroidBoyActivity : AppCompatActivity(), NoticeDialogListener {
    private var feedCategories: EnumSet<CardCategory>? = null
    private var drawerLayout: DrawerLayout? = null

    private val requestMultiplePermissionLauncher = registerForActivityResult(RequestMultiplePermissions()) { result: Map<String, Boolean> ->
        if (result.containsKey(Manifest.permission.ACCESS_FINE_LOCATION)
            && result[Manifest.permission.ACCESS_FINE_LOCATION] != true
        ) {
            showToast("Location permissions denied.")
        } else if (result.containsKey(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            && result[Manifest.permission.ACCESS_BACKGROUND_LOCATION] != true
        ) {
            showToast("Background location permissions denied.")
        } else {
            showToast("All required location permissions granted.")
        }
    }

    private val feedFragment: AppboyFeedFragment?
        get() {
            val fragments = supportFragmentManager.fragments
            for (i in fragments.indices) {
                if (fragments[i] is AppboyFeedFragment) {
                    return fragments[i] as AppboyFeedFragment?
                }
            }
            return null
        }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()
        val defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        if (defaultSharedPreferences.getBoolean("display_in_full_cutout_setting_key", false)) {
            setTheme(R.style.DisplayInNotchTheme)
            ViewUtils.enableImmersiveMode(window.decorView)
        }

        if (defaultSharedPreferences.getBoolean("display_no_limits_setting_key", false)) {
            ViewUtils.enableNoLimitsMode(window)
        }
        setContentView(R.layout.landing_page)
        val currentFragment = supportFragmentManager.findFragmentById(R.id.viewpager)
        brazelog(I) { "Creating DroidBoyActivity with current fragment: $currentFragment" }
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = null
        val viewPager = findViewById<ViewPager2>(R.id.viewpager)
        viewPager?.let { setupViewPager(it) }

        val tabLayout = findViewById<TabLayout>(R.id.tabs)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = (viewPager.adapter as Adapter).getTitle(position)
        }.attach()
        drawerLayout = findViewById(R.id.root)
        setupNewsFeedListener()
        brazelog(I) { "Braze device id is ${Braze.getInstance(applicationContext).deviceId}" }
    }

    private fun setupNewsFeedListener() {
        val newsFeedSharedPrefs = getSharedPreferences(getString(R.string.feed), MODE_PRIVATE)
        // We implement the listener this way so that it doesn't get garbage collected when we navigate to and from this activity
        val newsfeedSortListener =
            SharedPreferences.OnSharedPreferenceChangeListener { _: SharedPreferences?, _: String? ->
                val sharedPref1 = getSharedPreferences(getString(R.string.feed), MODE_PRIVATE)
                feedFragment?.let {
                    it.sortEnabled = sharedPref1.getBoolean(getString(R.string.sort_feed), false)
                }
            }
        newsFeedSharedPrefs.registerOnSharedPreferenceChangeListener(newsfeedSortListener)
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return

        val permissionsToRequest = mutableListOf(Manifest.permission.INTERNET)
        if (!didRequestLocationPermission) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val hasFineLocationPermission =
                    applicationContext.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                if (!hasFineLocationPermission) {
                    permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
                } else if (!applicationContext.hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    // Request background now that fine is set
                    permissionsToRequest.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val hasAllPermissions = (
                    applicationContext.hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        && applicationContext.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    )
                if (!hasAllPermissions) {
                    // Request both BACKGROUND and FINE location permissions
                    permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
                    permissionsToRequest.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
            } else {
                // From M to P, FINE gives us BACKGROUND access
                if (!applicationContext.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // Request only FINE location permission
                    permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
            didRequestLocationPermission = true
        }
        requestLocationPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                requestMultiplePermissionLauncher
            )
    }

    private fun setupViewPager(viewPager: ViewPager2) {
        viewPager.adapter = Adapter(supportFragmentManager, lifecycle, applicationContext)
    }

    public override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    public override fun onResume() {
        super.onResume()
        processIntent()
        val configurationProvider = BrazeConfigurationProvider(this)
        val endpoint = configurationProvider.customEndpoint.let {
            if (it.isNullOrEmpty()) {
                configurationProvider.baseUrlForRequests
            } else {
                it
            }
        }

        (findViewById<View>(R.id.toolbar_info_endpoint) as TextView).text = "endpoint: $endpoint"
        val configuredApiKey = Braze.getConfiguredApiKey(configurationProvider)
        (findViewById<View>(R.id.toolbar_info_api_key) as TextView).text = "current api key: $configuredApiKey"
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.actionbar_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.feed_activity_launch -> startActivity(Intent(this, FeedFragmentActivity::class.java))
            R.id.action_settings -> startActivity(Intent(this, SettingsActivity::class.java))
            R.id.geofences_map -> {
                drawerLayout?.closeDrawers()
                startActivity(Intent(applicationContext, GeofencesMapActivity::class.java))
            }
            R.id.iam_sandbox -> startActivity(Intent(applicationContext, InAppMessageSandboxActivity::class.java))
            R.id.feed_categories -> {
                val feedFragment = feedFragment
                if (feedFragment != null) {
                    val newFragment: DialogFragment = FeedCategoriesFragment.newInstance(feedFragment.categories ?: CardCategory.getAllCategories())
                    newFragment.show(supportFragmentManager, "categories")
                } else {
                    showToast("Feed fragment hasn't been instantiated yet.")
                }
            }
            R.id.action_flush -> {
                Braze.getInstance(this).requestContentCardsRefresh(false)
                Braze.getInstance(this).requestImmediateDataFlush()
                showToast("Requested data flush and content card sync.")
            }
            else -> brazelog(E) { "The ${item.title} options item was not found. Ignoring." }
        }
        return true
    }

    private fun replaceCurrentFragment(newFragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val currentFragment = fragmentManager.findFragmentById(R.id.root)
        if (currentFragment != null && currentFragment.javaClass == newFragment.javaClass) {
            brazelog(I) {
                "Fragment of type ${currentFragment.javaClass} is already the active fragment. Ignoring " +
                    "request to replace current fragment."
            }
            return
        }
        hideSoftKeyboard()
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.setCustomAnimations(
            android.R.anim.fade_in, android.R.anim.fade_out,
            android.R.anim.fade_in, android.R.anim.fade_out
        )
        fragmentTransaction.replace(R.id.root, newFragment, newFragment.javaClass.toString())
        if (currentFragment != null) {
            fragmentTransaction.addToBackStack(newFragment.javaClass.toString())
        } else {
            fragmentTransaction.addToBackStack(null)
        }
        fragmentTransaction.commit()
    }

    private fun processIntent() {
        // Check to see if the Activity was opened by the Broadcast Receiver. If it was, navigate to the
        // correct fragment.
        val extras = intent.extras
        if (extras != null && Constants.APPBOY == extras.getString(resources.getString(R.string.source_key))) {
            navigateToDestination(extras)
            val bundleLogString = bundleToLogString(extras)
            showToast(bundleLogString)
            brazelog { bundleLogString }
        }

        // Clear the intent so that screen rotations don't cause the intent to be re-executed on.
        intent = Intent()
    }

    private fun navigateToDestination(extras: Bundle) {
        // DESTINATION_VIEW holds the name of the fragment we're trying to visit.
        val destination = extras.getString(resources.getString(R.string.destination_view))
        if (resources.getString(R.string.feed_key) == destination) {
            val feedFragment = AppboyFeedFragment()
            feedFragment.categories = feedCategories
            replaceCurrentFragment(feedFragment)
        } else if (resources.getString(R.string.home) == destination) {
            replaceCurrentFragment(MainFragment())
        }
    }

    private fun hideSoftKeyboard() {
        currentFocus?.let {
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(
                it.windowToken,
                InputMethodManager.RESULT_UNCHANGED_SHOWN
            )
        }
    }

    override fun onDialogPositiveClick(dialog: FeedCategoriesFragment) {
        feedFragment?.let {
            feedCategories = EnumSet.copyOf(dialog.selectedCategories)
            it.categories = feedCategories
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Adapter that has all the information for the Fragments to feed the ViewPager2
     */
    internal class Adapter(fm: FragmentManager, lifecycle: Lifecycle, val context: Context) :
        FragmentStateAdapter(fm, lifecycle) {
        data class FragmentInfo(
            val fragmentConstructor: () -> Fragment,
            val title: String
        )

        private val fragmentInfo = arrayOf(
            FragmentInfo(
                { MainFragment() },
                "Main"
            ),
            FragmentInfo(
                { InAppMessageTesterFragment() },
                context.getString(R.string.inappmessage_tester_tab_title)
            ),
            FragmentInfo(
                { ContentCardsFragment() },
                "Content Cards"
            ),
            FragmentInfo(
                { PushTesterFragment() },
                "Push"
            ),
            FragmentInfo(
                { SettingsFragment() },
                context.getString(R.string.settings_fragment_tab_title)
            )
        )

        override fun getItemCount() = fragmentInfo.size

        override fun createFragment(position: Int) = fragmentInfo[position].fragmentConstructor.invoke()

        fun getTitle(position: Int) = fragmentInfo[position].title
    }

    companion object {
        private var didRequestLocationPermission = false

        private fun bundleToLogString(bundle: Bundle): String {
            val bundleString = StringBuilder()
            bundleString.append("Received intent with extras Bundle of size ${bundle.size()} from Braze containing [")
            for (key in bundle.keySet()) {
                bundleString.append(" '$key':'${bundle[key]}'")
            }
            bundleString.append(" ].")
            return bundleString.toString()
        }
    }
}
