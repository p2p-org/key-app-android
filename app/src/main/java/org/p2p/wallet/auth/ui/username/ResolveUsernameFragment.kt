package org.p2p.wallet.auth.ui.username

import android.os.Bundle
import android.view.View
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.utils.edgetoedge.Edge
import org.p2p.wallet.utils.edgetoedge.edgeToEdge
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.koin.android.ext.android.inject
import org.p2p.wallet.databinding.FragmentResolveUsernameBinding

class ResolveUsernameFragment :
    BaseMvpFragment<ResolveUsernameContract.View,
        ResolveUsernameContract.Presenter>(R.layout.fragment_resolve_username),
    ResolveUsernameContract.View {

    companion object {
        fun create() = ResolveUsernameFragment()
    }

    override val presenter: ResolveUsernameContract.Presenter by inject()

    private val binding: FragmentResolveUsernameBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            edgeToEdge {
                toolbar.fit { Edge.TopArc }
            }
            toolbar.setNavigationOnClickListener { popBackStack() }
        }
    }
}