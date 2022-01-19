package org.p2p.wallet.auth.ui.security

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.verify.VerifySecurityKeyFragment
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSecurityKeyBinding
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.attachAdapter
import org.p2p.wallet.utils.copyToClipBoard
import org.p2p.wallet.utils.toast
import org.p2p.wallet.utils.edgetoedge.Edge
import org.p2p.wallet.utils.edgetoedge.edgeToEdge
import org.p2p.wallet.utils.viewbinding.viewBinding

class SecurityKeyFragment :
    BaseMvpFragment<SecurityKeyContract.View, SecurityKeyContract.Presenter>(R.layout.fragment_security_key),
    SecurityKeyContract.View {

    companion object {
        fun create() = SecurityKeyFragment()
    }

    override val presenter: SecurityKeyContract.Presenter by inject()

    private val binding: FragmentSecurityKeyBinding by viewBinding()

    private val keysAdapter: KeysAdapter by lazy {
        KeysAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            edgeToEdge {
                toolbar.fit { Edge.TopArc }
                termsAndConditionsTextView.fitMargin { Edge.BottomArc }
            }
            toolbar.setNavigationOnClickListener { popBackStack() }
            nextButton.setOnClickListener {
                presenter.cacheKeys()
            }
            renewButton.setOnClickListener {
                presenter.loadKeys()
            }
            copyButton.setOnClickListener {
                presenter.copyKeys()
            }

            with(keysRecyclerView) {
                attachAdapter(keysAdapter)
                layoutManager = GridLayoutManager(requireContext(), 3)
            }
        }
    }

    override fun showKeys(keys: List<String>) {
        keysAdapter.setItems(keys)
    }

    override fun showLoading(isLoading: Boolean) {
        binding.progressView.isVisible = isLoading
    }

    override fun navigateToVerify(keys: List<String>) {
        replaceFragment(VerifySecurityKeyFragment.create(keys))
    }

    override fun copyToClipboard(keys: List<String>) {
        if (keys.isNotEmpty()) {
            val data = keys.joinToString(separator = " ")
            requireContext().copyToClipBoard(data)
            toast(R.string.common_copied)
        }
    }
}