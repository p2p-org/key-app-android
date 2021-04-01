package com.p2p.wowlet.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.p2p.wowlet.R
import com.p2p.wowlet.fragment.dashboard.dialog.ProfileDetailsFragment
import com.p2p.wowlet.fragment.dashboard.view.DashboardFragment
import com.p2p.wowlet.fragment.investments.view.InvestmentsFragment
import com.p2p.wowlet.fragment.search.view.SearchFragment
import com.p2p.wowlet.utils.replace
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.bottom_navigation_dashboard -> replace(DashboardFragment.create(false), addToBackStack = false)
                R.id.bottom_navigation_investments -> replace(InvestmentsFragment(), addToBackStack = false)
                R.id.bottom_navigation_search -> replace(SearchFragment(), addToBackStack = false)
                R.id.bottom_navigation_profile -> replace(ProfileDetailsFragment.newInstance(), addToBackStack = false)
            }
            return@setOnNavigationItemSelectedListener true
        }
        bottomNavigation.selectedItemId = R.id.bottom_navigation_dashboard
    }
}