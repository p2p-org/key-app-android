package com.p2p.wowlet.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.p2p.wowlet.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    lateinit var navHostFragment: NavHostFragment
    private val navOptions = NavOptions.Builder().setLaunchSingleTop(true).build()
    val navController by lazy { findNavController(R.id.mainContainer) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initNavigationController()
    }

    private fun initNavigationController() {
        navController.addOnDestinationChangedListener { controller: NavController?, destination: NavDestination?, arguments: Bundle? ->
            showHideNav(
                false
            )
        }
        bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.bottom_navigation_home -> {
                    navController.navigate(R.id.navigation_dashboard, null,navOptions )
                }

                R.id.bottom_navigation_investments -> {
                    navController.navigate(R.id.navigation_investments, null,navOptions )
                }
                R.id.bottom_navigation_search -> {
                    navController.navigate(R.id.navigation_search, null,navOptions )
                }
                R.id.bottom_navigation_profile -> {
                    navController.navigate(R.id.navigation_profile, null,navOptions )
                }

            }
            return@setOnNavigationItemSelectedListener true;
        }
    }

    fun showHideNav(showHide: Boolean) {
        if (showHide) {
            if (!bottomNavigation.isShown) {
                bottomNavigation.visibility = View.VISIBLE
            }
        } else {
            bottomNavigation.visibility = View.GONE
        }


    }
}