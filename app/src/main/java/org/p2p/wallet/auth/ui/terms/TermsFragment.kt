package org.p2p.wallet.auth.ui.terms

import android.os.Bundle
import android.view.View
import org.p2p.wallet.utils.edgetoedge.edgeToEdge
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.security.SecurityKeyFragment
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentTermsBinding
import org.p2p.wallet.utils.edgetoedge.Edge
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding

class TermsFragment : BaseFragment(R.layout.fragment_terms) {

    private val binding: FragmentTermsBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            edgeToEdge {
                contentView.fitPadding { Edge.All }
            }
            toolbar.setNavigationOnClickListener { popBackStack() }

            acceptButton.clipToOutline = true
            acceptButton.setOnClickListener {
                popAndReplaceFragment(SecurityKeyFragment.create())
            }
        }
    }
}