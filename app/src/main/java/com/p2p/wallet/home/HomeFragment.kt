package com.p2p.wallet.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.kp.kompanion.utils.edgetoedge.edgeToEdge
import com.p2p.wallet.R
import com.p2p.wallet.dashboard.ui.dialog.ProfileDetailsFragment
import com.p2p.wallet.dashboard.ui.view.DashboardFragment
import com.p2p.wallet.databinding.FragmentHomeBinding
import com.p2p.wallet.investments.InvestmentsFragment
import com.p2p.wallet.search.SearchFragment
import com.p2p.wallet.utils.edgetoedge.Edge
import com.p2p.wallet.utils.replaceFragment
import com.p2p.wallet.utils.viewbinding.viewBinding

class HomeFragment : Fragment(R.layout.fragment_home) {

    companion object {
        fun create() = HomeFragment()
    }

    private val binding: FragmentHomeBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            edgeToEdge {
                contentView.fit { Edge.All }
            }
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