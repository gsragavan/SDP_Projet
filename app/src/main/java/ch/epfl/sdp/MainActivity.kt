package ch.epfl.sdp

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import ch.epfl.sdp.SearchGroupSelectionActivity.Companion.CANCELLED_RESULT_TAG
import ch.epfl.sdp.SearchGroupSelectionActivity.Companion.SEARH_GROUP_ID_SELECTION_RESULT_TAG
import ch.epfl.sdp.drone.Drone
import ch.epfl.sdp.utils.Auth
import ch.epfl.sdp.utils.CentralLocationManager
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var snackbar: Snackbar

    companion object {
        private val SEARCH_GROUP_SELECTION_ACTIVITY_REQUEST_CODE = 7865
    }

    private var selectSearchGroupAction = false

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val navView = findViewById<NavigationView>(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        snackbar = Snackbar.make(navView, R.string.not_connected_message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(Color.BLACK).setTextColor(Color.WHITE)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
                setOf(R.id.nav_home, R.id.nav_maps_managing, R.id.nav_signout_button),
                drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        Auth.loggedIn.observe(this, Observer {
            navView.menu.findItem(R.id.nav_signout_button).isVisible = it
            navView.menu.findItem(R.id.nav_signout_button).isEnabled = it
        })

        navView.menu.findItem(R.id.nav_signout_button).setOnMenuItemClickListener {
            Auth.logout()
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    override fun onStart() {
        super.onStart()
        CentralLocationManager.configure(this)
        showSnackbar()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.activity_main_settings, menu)
        return true
    }

    // Opens the drawer instead of navigating up
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun openSettings(menuItem: MenuItem?) {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        CentralLocationManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun showSnackbar() {
        if (!Drone.isDroneConnected())
            snackbar.show()
    }

    fun selectSearchGroup(view: View) {
        if (Auth.loggedIn.value == false) {
            selectSearchGroupAction = true
            Auth.login(this) { success ->
                if (success) {
                    selectSearchGroup(view)
                }
            }
        } else {
            val intent = Intent(this, SearchGroupSelectionActivity::class.java)
            startActivityForResult(intent, SEARCH_GROUP_SELECTION_ACTIVITY_REQUEST_CODE)
        }
    }

    fun startMission(view: View) {
        val intent = Intent(this, MapActivity::class.java)
                .putExtra("groupId", "g2") //TODO adapt group ID via group choosing activity or something
        startActivity(intent)
    }

    fun workOffline(view: View) {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == SEARCH_GROUP_SELECTION_ACTIVITY_REQUEST_CODE) {
            val cancelled = data!!.getBooleanExtra(CANCELLED_RESULT_TAG, true)
            if (!cancelled) {
                val groupId = data.getStringExtra(SEARH_GROUP_ID_SELECTION_RESULT_TAG)
                PreferenceManager.getDefaultSharedPreferences(this)
                        .edit()
                        .putString("CURRENT_SEARCHGROUP_ID", groupId)
                        .apply()
            }
        }
        Auth.onActivityResult(requestCode, resultCode, data)
    }
}
