package org.p2p.wallet.auth.ui.done

import android.os.Bundle
import android.view.View
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentAuthDoneBinding
import org.p2p.wallet.main.ui.main.MainFragment
import org.p2p.wallet.utils.edgetoedge.Edge
import org.p2p.wallet.utils.edgetoedge.edgeToEdge
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding

class AuthDoneFragment : BaseFragment(R.layout.fragment_auth_done) {

    companion object {
        fun create() = AuthDoneFragment()
    }

    private val binding: FragmentAuthDoneBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            edgeToEdge {
                finishButton.fitMargin { Edge.BottomArc }
            }

            finishButton.clipToOutline = true
            finishButton.setOnClickListener {
                popAndReplaceFragment(MainFragment.create(), inclusive = true)
            }
        }
    }
}