package com.p2p.wowlet.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.p2p.wowlet.R
import com.p2p.wowlet.fragment.dashboard.dialog.ProfileDetailsFragment
import com.p2p.wowlet.fragment.dashboard.view.DashboardFragment
import com.p2p.wowlet.fragment.investments.view.InvestmentsFragment
import com.p2p.wowlet.fragment.search.view.SearchFragment
import com.p2p.wowlet.utils.replaceFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.bottom_navigation_dashboard -> replaceFragment(DashboardFragment.create(false), addToBackStack = false)
                R.id.bottom_navigation_investments -> replaceFragment(InvestmentsFragment(), addToBackStack = false)
                R.id.bottom_navigation_search -> replaceFragment(SearchFragment(), addToBackStack = false)
                R.id.bottom_navigation_profile -> replaceFragment(ProfileDetailsFragment.newInstance(), addToBackStack = false)
            }
            return@setOnNavigationItemSelectedListener true
        }
        bottomNavigation.selectedItemId = R.id.bottom_navigation_dashboard
    }
}