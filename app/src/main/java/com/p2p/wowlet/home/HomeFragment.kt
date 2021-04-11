package com.p2p.wowlet.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.p2p.wowlet.R
import com.p2p.wowlet.dashboard.dialog.ProfileDetailsFragment
import com.p2p.wowlet.dashboard.view.DashboardFragment
import com.p2p.wowlet.databinding.FragmentHomeBinding
import com.p2p.wowlet.investments.view.InvestmentsFragment
import com.p2p.wowlet.search.SearchFragment
import com.p2p.wowlet.utils.replaceFragment
import com.p2p.wowlet.utils.viewbinding.viewBinding

class HomeFragment : Fragment(R.layout.fragment_home) {

    companion object {
        fun create() = HomeFragment()
    }

    private val binding: FragmentHomeBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            bottomNavigation.setOnNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.bottom_navigation_dashboard -> navigate(DashboardFragment.create(false))
                    R.id.bottom_navigation_investments -> navigate(InvestmentsFragment())
                    R.id.bottom_navigation_search -> navigate(SearchFragment())
                    R.id.bottom_navigation_profile -> navigate(ProfileDetailsFragment.newInstance())
                }
                return@setOnNavigationItemSelectedListener true
            }
            bottomNavigation.selectedItemId = R.id.bottom_navigation_dashboard
        }
    }

    private fun navigate(fragment: Fragment) {
        replaceFragment(fragment, containerId = R.id.container, addToBackStack = false)
    }
}