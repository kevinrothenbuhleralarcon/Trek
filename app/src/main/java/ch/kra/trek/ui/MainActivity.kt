package ch.kra.trek.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import ch.kra.trek.R
import ch.kra.trek.other.Constants.ACTION_SHOW_TREK_FRAGMENT

class MainActivity : AppCompatActivity() {
    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        setupActionBarWithNavController(navController)
        navigateToTrekFragmentIfNeeded(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d("service", "New Intent")
        navigateToTrekFragmentIfNeeded(intent)
    }

    private fun navigateToTrekFragmentIfNeeded(intent: Intent?) {
        if (intent?.action == ACTION_SHOW_TREK_FRAGMENT) {
            Log.d("service", "Navigate to fragment")
            navController.navigate(R.id.action_global_trekFragment)
        }
        Log.d("service", "No need to navigate to new fragment")
    }
}