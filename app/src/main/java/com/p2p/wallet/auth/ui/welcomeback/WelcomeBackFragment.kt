package com.p2p.wallet.auth.ui.welcomeback

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.p2p.wallet.R
import com.p2p.wallet.common.mvp.BaseFragment
import com.p2p.wallet.databinding.FragmentAuthDoneBinding
import com.p2p.wallet.home.HomeFragment
import com.p2p.wallet.utils.edgetoedge.Edge
import com.p2p.wallet.utils.edgetoedge.edgeToEdge
import com.p2p.wallet.utils.popAndReplaceFragment
import com.p2p.wallet.utils.viewbinding.viewBinding

class WelcomeBackFragment : BaseFragment(R.layout.fragment_auth_done) {

    companion object {
        fun create() = WelcomeBackFragment()
    }

    private val binding: FragmentAuthDoneBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            edgeToEdge {
                finishButton.fitMargin { Edge.BottomArc }
            }

            titleTextView.setText(R.string.auth_welcome_back)
            subTitleTextView.isVisible = false

            finishButton.clipToOutline = true
            finishButton.setOnClickListener {
                popAndReplaceFragment(HomeFragment.create(), inclusive = true)
            }
        }
    }
}