package com.p2p.wallet.auth.ui.onboarding

import android.os.Bundle
import android.view.View
import com.p2p.wallet.R
import com.p2p.wallet.auth.ui.security.SecurityKeyFragment
import com.p2p.wallet.auth.ui.username.ReservingUsernameFragment
import com.p2p.wallet.common.mvp.BaseFragment
import com.p2p.wallet.databinding.FragmentOnboardingBinding
import com.p2p.wallet.restore.ui.main.RestoreFragment
import com.p2p.wallet.utils.edgetoedge.Edge
import com.p2p.wallet.utils.edgetoedge.edgeToEdge
import com.p2p.wallet.utils.replaceFragment
import com.p2p.wallet.utils.viewbinding.viewBinding

class OnboardingFragment : BaseFragment(R.layout.fragment_onboarding) {

    companion object {
        fun create() = OnboardingFragment()
    }

    private val binding: FragmentOnboardingBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            edgeToEdge {
                loginButton.fitMargin { Edge.BottomArc }
            }

            createButton.clipToOutline = true
            createButton.setOnClickListener {
                replaceFragment(SecurityKeyFragment.create())
            }
            loginButton.setOnClickListener {
                replaceFragment(RestoreFragment.create())
            }
        }
    }
}