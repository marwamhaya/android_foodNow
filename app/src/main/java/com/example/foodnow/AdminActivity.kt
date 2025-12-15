package com.example.foodnow

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import android.content.Intent


class AdminActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_admin)
        setSupportActionBar(toolbar)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_admin) as androidx.navigation.fragment.NavHostFragment
        val navController = navHostFragment.navController
        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_nav_admin)
        
        androidx.navigation.ui.NavigationUI.setupWithNavController(bottomNav, navController)
        androidx.navigation.ui.NavigationUI.setupActionBarWithNavController(this, navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = androidx.navigation.Navigation.findNavController(this, R.id.nav_host_fragment_admin)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_admin_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        val app = application as FoodNowApp
        app.repository.logout()
        val intent = Intent(this, MainActivity::class.java) // Or MainActivity if that's the entry point
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
