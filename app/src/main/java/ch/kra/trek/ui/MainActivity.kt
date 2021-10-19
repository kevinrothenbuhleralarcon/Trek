package ch.kra.trek.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import ch.kra.trek.R
import ch.kra.trek.databinding.ActivityMainBinding
import ch.kra.trek.other.Constants.ACTION_SHOW_TREK_FRAGMENT

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNav.setupWithNavController(navController)

        setupActionBarWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loadTrekFragment, R.id.trekFragment, R.id.settingsFragment -> {
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                    binding.bottomNav.visibility = View.VISIBLE
                }

                else -> {
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                    binding.bottomNav.visibility = View.GONE
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrekFragmentIfNeeded(intent)
    }

    private fun navigateToTrekFragmentIfNeeded(intent: Intent?) {
        if (intent?.action == ACTION_SHOW_TREK_FRAGMENT) {
            navController.navigate(R.id.action_global_trekFragment)
        }
    }
}