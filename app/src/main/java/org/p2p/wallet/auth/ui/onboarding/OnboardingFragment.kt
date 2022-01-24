package org.p2p.wallet.auth.ui.onboarding

import android.os.Bundle
import android.view.View
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.pin.create.CreatePinFragment
import org.p2p.wallet.auth.ui.pin.create.PinLaunchMode
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentOnboardingBinding
import org.p2p.wallet.restore.ui.keys.SecretKeyFragment
import org.p2p.wallet.utils.edgetoedge.Edge
import org.p2p.wallet.utils.edgetoedge.edgeToEdge
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding

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
                replaceFragment(CreatePinFragment.create(PinLaunchMode.CREATE))
            }
            loginButton.setOnClickListener {
                replaceFragment(SecretKeyFragment.create())
            }
        }
    }
}